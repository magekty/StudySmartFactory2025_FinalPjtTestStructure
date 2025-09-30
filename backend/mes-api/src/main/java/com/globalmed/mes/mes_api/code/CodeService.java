package com.globalmed.mes.mes_api.code;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeRepo repo;

    // group_code::code => code_id 캐시
    private final Map<String, Long> idCache = new ConcurrentHashMap<>();
    private final Map<Long, String> codeCache = new ConcurrentHashMap<>();

    private static String key(String groupCode, String code) {
        // 공백/대소문자 이슈 방지
        return (groupCode == null ? "" : groupCode.trim().toUpperCase())
                + "::"
                + (code == null ? "" : code.trim().toUpperCase());
    }

    public CodeEntity getCode(String groupCode, String code) {
        return repo.findByGroupCodeAndCodeAndUseYn(groupCode, code, 'Y')
                .orElseThrow(() -> new IllegalStateException("CODE_NOT_FOUND"));
    }

    /**
     * 활성 코드(use_yn='Y')의 code_id 반환. 없으면 IllegalArgumentException.
     * 예) idOf("CMMS_WO_STATUS", "OPEN")
     */
    @Transactional(readOnly = true)
    public Long idOf(String groupCode, String code) {
        String k = key(groupCode, code);
        return idCache.computeIfAbsent(k, _k ->
                repo.findByGroupCodeAndCodeAndUseYn(groupCode, code, 'Y')
                        .map(CodeEntity::getCodeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Code not found or inactive: group=" + groupCode + ", code=" + code))
        );
    }

    /**
     * 없으면 Optional.empty() 반환 (필요시 사용)
     */
    @Transactional(readOnly = true)
    public String codeOf(String expectedGroup, Long codeId) {
        if (codeId == null) throw new IllegalArgumentException("codeId is null");
        return codeCache.computeIfAbsent(codeId, id ->
                repo.findById(id)
                        .filter(e -> e.getUseYn() == 'Y')
                        .filter(e -> expectedGroup == null || expectedGroup.equalsIgnoreCase(e.getGroupCode()))
                        .map(CodeEntity::getCode)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Code not found/disabled or group mismatch: id=" + id + ", group=" + expectedGroup))
        );
    }



    /** 특정 키만 캐시 제거 (코드 테이블 변경 후 수동 무효화용) */
    public void evict(String groupCode, String code) {
        idCache.remove(key(groupCode, code));
    }

    /** 전체 캐시 삭제 */
    public void clearCache() {
        idCache.clear();
    }
}