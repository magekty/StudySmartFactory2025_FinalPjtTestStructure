package com.globalmed.mes.mes_api.process.service;

import com.globalmed.mes.mes_api.code.dto.CertDto;
import com.globalmed.mes.mes_api.code.dto.EquipmentDto;
import com.globalmed.mes.mes_api.employee.cert.domain.CertEntity;
import com.globalmed.mes.mes_api.employee.cert.domain.ProcessCertEntity;
import com.globalmed.mes.mes_api.employee.cert.repository.CertRepo;
import com.globalmed.mes.mes_api.employee.cert.repository.ProcessCertRepo;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentRepo;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.process.dto.ProcessCreationDto;
import com.globalmed.mes.mes_api.process.dto.ProcessDetailDto;
import com.globalmed.mes.mes_api.process.dto.ProcessListDto;
import com.globalmed.mes.mes_api.process.repository.ProcessRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepo processRepo;
    private final EquipmentRepo equipmentRepo;
    private final ProcessCertRepo proCertRepo;
    private final CertRepo certRepo;

    @Transactional
    public Page<ProcessListDto> getProcessList(Pageable pageable) {
        return processRepo.findAllByIsDeletedFalse(pageable)
                .map(entity -> new ProcessListDto(entity.getProcessId(),entity.getProcessName(), entity.getDescription()));
    }

    @Transactional
    public ProcessDetailDto getProcessDetail(String processId) {
        ProcessEntity process = processRepo.findById(processId)
                .orElse(null);
        if (process == null) {
            return null;
        }

        List<EquipmentEntity> equipments = equipmentRepo.findEquipByPro(processId);
        List<ProcessCertEntity> processCerts = proCertRepo.findProCert(processId);

        List<EquipmentDto> equipmentDtos = equipments.stream()
                .map(EquipmentDto::fromEntity)
                .collect(Collectors.toList());

        List<CertDto> requiredCertDtos = processCerts.stream()
                .map(pc -> CertDto.fromEntity(pc.getCert()))
                .collect(Collectors.toList());

        OffsetDateTime lastModAt = null;
        String lastModBy = null;

        if (process.getModifiedAt() != null && process.getModifiedBy() != null) {
            lastModAt = process.getModifiedAt().atOffset(ZoneOffset.UTC);
            lastModBy = process.getModifiedBy();
        } else if (process.getCreatedAt() != null && process.getCreatedBy() != null) {
            lastModAt = process.getCreatedAt().atOffset(ZoneOffset.UTC);
            lastModBy = process.getCreatedBy();
        }

        return new ProcessDetailDto(
                process.getProcessId(),
                process.getProcessName(),
                process.getDescription(),
                equipmentDtos,
                requiredCertDtos,
                lastModAt,
                lastModBy
        );
    }

    @Transactional
    public ProcessDetailDto createProcess(ProcessCreationDto creationDto) {
        Optional<ProcessEntity> existingProcessOpt = processRepo.findByProcessName(creationDto.processName());

        if (existingProcessOpt.isPresent()) {
            ProcessEntity existingProcess = existingProcessOpt.get();

            if (!existingProcess.isDeleted()) {
                throw new IllegalArgumentException("동일한 이름의 공정이 이미 존재합니다.");
            }

            existingProcess.setDeleted(false);
            existingProcess.setDescription(creationDto.description());
            ProcessEntity revivedProcess = processRepo.save(existingProcess);

            updateProcessCerts(revivedProcess, creationDto.requiredCertCodes());

            return toProcessDetailDto(revivedProcess, getCertDtosForProcess(revivedProcess));
        }


        ProcessEntity newProcess = new ProcessEntity();
        newProcess.setProcessId(UUID.randomUUID().toString());
        newProcess.setProcessName(creationDto.processName());
        newProcess.setDescription(creationDto.description());
        newProcess.setCreatedAt(LocalDateTime.now());
        newProcess.setCreatedBy("system");

        ProcessEntity savedProcess = processRepo.save(newProcess);

        updateProcessCerts(savedProcess, creationDto.requiredCertCodes());

        return toProcessDetailDto(savedProcess, getCertDtosForProcess(savedProcess));
    }

    @Transactional
    public ProcessDetailDto updateProcess(String processId, ProcessCreationDto creationDto) {
        ProcessEntity process = processRepo.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("해당 공정을 찾을 수 없습니다: " + processId));

        // 이름 중복 체크 (자기 자신 제외)
        if (processRepo.existsByProcessName(creationDto.processName()) &&
                !process.getProcessName().equals(creationDto.processName())) {
            throw new IllegalArgumentException("동일한 이름의 공정이 이미 존재합니다.");
        }

        // 값 업데이트
        process.setProcessName(creationDto.processName());
        process.setDescription(creationDto.description());
        process.setModifiedAt(LocalDateTime.now());

        // 자격증 갱신
        updateProcessCerts(process, creationDto.requiredCertCodes());

        // DTO 변환
        List<CertDto> requiredCertDtos = getCertDtosForProcess(process);

        return new ProcessDetailDto(
                process.getProcessId(),
                process.getProcessName(),
                process.getDescription(),
                equipmentRepo.findEquipByPro(processId).stream()
                        .map(EquipmentDto::fromEntity)
                        .collect(Collectors.toList()),
                requiredCertDtos,
                process.getModifiedAt() != null ? process.getModifiedAt().atOffset(ZoneOffset.UTC)
                        : process.getCreatedAt().atOffset(ZoneOffset.UTC),
                process.getModifiedBy() != null ? process.getModifiedBy() : process.getCreatedBy()
        );
    }


    private void updateProcessCerts(ProcessEntity process, List<String> certCodes) {
        // 1. 현재 공정의 모든 자격증 매핑 (삭제된 것 포함)을 가져옵니다.

        List<ProcessCertEntity> existingCerts = proCertRepo.findAllByProcess(process);
        Map<String, ProcessCertEntity> certCodeMap = existingCerts.stream()
                .collect(Collectors.toMap(pc -> pc.getCert().getCertCode(), pc -> pc));

        Set<String> newCertCodes = certCodes.stream().collect(Collectors.toSet());
        List<ProcessCertEntity> entitiesToSave = new java.util.ArrayList<>();

        // 2. 새로운 자격증 목록을 순회하며 추가 및 업데이트할 엔티티를 결정합니다.
        for (String certCode : newCertCodes)
        {
            CertEntity cert = certRepo.findByCertCode(certCode)
                    .orElseThrow(() -> new IllegalArgumentException("자격증 Code를 찾을 수 없습니다: " + certCode));

            if (certCodeMap.containsKey(certCode)) {
                // 기존에 존재하면 (소프트 삭제 여부와 상관없이)
                ProcessCertEntity existingEntity = certCodeMap.get(certCode);
                if (existingEntity.isDeleted()) {
                    // 소프트 삭제된 상태라면 다시 활성화
                    existingEntity.setDeleted(false);
                    existingEntity.setDeletedAt(null);
                    entitiesToSave.add(existingEntity);
                }
                certCodeMap.remove(certCode);
            } else {
                // 기존에 존재하지 않으면 새로운 엔티티 생성
                ProcessCertEntity newProcessCert
                        = new ProcessCertEntity();
                newProcessCert.setProcess(process);
                newProcessCert.setCert(cert);
                entitiesToSave.add(newProcessCert);
            }
        }

        // 3. 새로운 목록에 포함되지 않는 기존 매핑(certCodeMap에 남은 것)을 소프트 삭제합니다.
        for (ProcessCertEntity existingEntity : certCodeMap.values()) {
            if (!existingEntity.isDeleted()) {
                existingEntity.setDeleted(true);
                existingEntity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
                entitiesToSave.add(existingEntity);
            }
        }

        proCertRepo.saveAll(entitiesToSave);

    }


    private List<CertDto> getCertDtosForProcess(ProcessEntity process) {
        List<ProcessCertEntity> processCerts = proCertRepo.findProCert(process.getProcessId());
        return processCerts.stream()
                .map(pc -> CertDto.fromEntity(pc.getCert()))
                .collect(Collectors.toList());
    }

    private ProcessDetailDto toProcessDetailDto(ProcessEntity process, List<CertDto> certs) {
        OffsetDateTime lastModAt = null;
        String lastModBy = null;

        if (process.getModifiedAt() != null && process.getModifiedBy() != null) {
            lastModAt = process.getModifiedAt().atOffset(ZoneOffset.UTC);
            lastModBy = process.getModifiedBy();
        } else if (process.getCreatedAt() != null && process.getCreatedBy() != null) {
            lastModAt = process.getCreatedAt().atOffset(ZoneOffset.UTC);
            lastModBy = process.getCreatedBy();
        }

        return new ProcessDetailDto(
                process.getProcessId(),
                process.getProcessName(),
                process.getDescription(),
                List.of(),
                certs,
                lastModAt,
                lastModBy
        );
    }
}
