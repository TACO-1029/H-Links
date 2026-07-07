(function () {
  const chartElement = document.getElementById('myCompetencyRadarChart');
  const dataElement = document.getElementById('myCompetencyRadarData');

  if (!chartElement || !dataElement || !window.echarts) {
    return;
  }

  const chartData = readChartData(dataElement);
  const chart = window.echarts.init(chartElement);
  chart.setOption(buildRadarOption(chartData));

  window.addEventListener('resize', () => chart.resize());

  function readChartData(element) {
    try {
      return JSON.parse(element.textContent || '{}');
    } catch (error) {
      return {};
    }
  }

  function buildRadarOption(data) {
    const labels = Array.isArray(data.labels) ? data.labels : [];
    const userScores = normalizeScores(data.userScores);
    const organizationScores = normalizeScores(data.organizationScores);

    return {
      color: ['#009E7A', '#CBD5E1'],
      animationDuration: 500,
      textStyle: {
        color: '#334155',
        fontFamily: 'Pretendard, Noto Sans KR, sans-serif',
      },
      tooltip: {
        trigger: 'item',
        confine: true,
        backgroundColor: '#0F172A',
        borderWidth: 0,
        textStyle: {
          color: '#FFFFFF',
          fontSize: 12,
        },
        valueFormatter: (value) => `${value}점`,
      },
      radar: {
        radius: '66%',
        center: ['50%', '48%'],
        splitNumber: 4,
        indicator: labels.map((label) => ({
          name: label,
          max: 100,
        })),
        axisName: {
          color: '#334155',
          fontSize: 12,
          fontWeight: 700,
          lineHeight: 16,
        },
        axisNameGap: 18,
        splitLine: {
          lineStyle: { color: '#CBD5E1' },
        },
        splitArea: {
          areaStyle: {
            color: ['rgba(0, 158, 122, 0.03)', 'rgba(0, 158, 122, 0.07)'],
          },
        },
        axisLine: {
          lineStyle: { color: '#CBD5E1' },
        },
      },
      series: [{
        type: 'radar',
        data: [
          {
            name: '조직 평균',
            value: organizationScores,
            areaStyle: {
              color: 'rgba(148, 163, 184, 0.1)',
            },
            lineStyle: {
              color: '#CBD5E1',
              type: 'dashed',
              width: 2,
            },
            itemStyle: {
              color: '#CBD5E1',
            },
          },
          {
            name: '내 역량',
            value: userScores,
            areaStyle: {
              color: 'rgba(0, 158, 122, 0.2)',
            },
            lineStyle: {
              width: 3,
            },
            itemStyle: {
              color: '#009E7A',
            },
          },
        ],
      }],
    };
  }

  function normalizeScores(scores) {
    if (!Array.isArray(scores)) {
      return [];
    }

    return scores.map((score) => {
      const value = Number(score);
      if (!Number.isFinite(value)) {
        return 0;
      }
      return Math.max(0, Math.min(100, value));
    });
  }
})();
