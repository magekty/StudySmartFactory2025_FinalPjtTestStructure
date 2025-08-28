import matplotlib.pyplot as plt
import numpy as np
from matplotlib import font_manager, rc

# 폰트 경로 지정 (시스템에 따라 다를 수 있음)
# Windows
font_path = "C:/Windows/Fonts/malgun.ttf"  # 맑은 고딕
# macOS
# font_path = "/Library/Fonts/AppleGothic.ttf"
# Linux (Ubuntu)
# font_path = "/usr/share/fonts/truetype/nanum/NanumGothic.ttf"

# 폰트 이름 가져오기
font_name = font_manager.FontProperties(fname=font_path).get_name()
rc('font', family=font_name)

# 마이너스 기호 깨짐 방지
plt.rcParams['axes.unicode_minus'] = False
# 준비 요소
factors = [
    "팀 구성", 
    "개발 기술스택", 
    "업무도메인 이해", 
    "프로젝트 관리", 
    "보안 및 인프라", 
    "운영/테스트 계획"
]

# 각 요소별 준비 수준 (0~100)
scores = [80, 75, 60, 70, 55, 50]

# -------- 막대 그래프 --------
plt.figure(figsize=(10,5))
bars = plt.bar(factors, scores)
plt.ylim(0,100)
plt.title("MES 개발 준비 현황 (막대그래프)", fontsize=14)
plt.ylabel("준비 수준 (%)")

# 부족한 부분은 빨간색 강조
for bar, score in zip(bars, scores):
    if score < 60:
        bar.set_color("red")
    else:
        bar.set_color("skyblue")

# -------- 레이더 차트 --------
plt.figure(figsize=(6,6))
angles = np.linspace(0, 2*np.pi, len(factors), endpoint=False).tolist()
scores_cycle = scores + [scores[0]]
angles_cycle = angles + [angles[0]]

ax = plt.subplot(111, polar=True)
ax.plot(angles_cycle, scores_cycle, 'o-', linewidth=2, label="준비 수준")
ax.fill(angles_cycle, scores_cycle, alpha=0.25)

ax.set_thetagrids(np.degrees(angles), factors)
ax.set_ylim(0,100)
plt.title("MES 개발 준비 현황 (레이더차트)", fontsize=14)
plt.legend(loc="upper right")

plt.show()
