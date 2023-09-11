/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const rawCoverageData = {}

document.addEventListener("DOMContentLoaded", event => {
    const summary = document.getElementById("summary")
    const statistics_container = summary.childNodes.item(3)

    const coverage_container = create_coverage_row()
    summary.insertBefore(coverage_container, statistics_container)

    const coverageChartCtx = document.getElementById('coverageChart');

    const coverageData = createRequirementCoverageData()

    const reqMap = getCoveredRequirementsMap()

    const coverageChart = new Chart(coverageChartCtx, {
        type: 'bar',
        data: coverageData,
        options: {
            responsive: true,
            skipNull: true,
            scales: {
                x: {
                    stacked: true,
                },
                y: {
                    type: 'logarithmic',
                    stacked: true,
                }
            },
            plugins: {
                // Change options for ALL labels of THIS CHART
                datalabels: {
                    color: '#444444', 'font.weight': 'bold',
                    formatter: (value, ctx) => {
                        if (value === 0) {
                            return '';
                        } else {
                            return value;
                        }
                    },
                },
                tooltip: {
                    callbacks: {
                        title: ctx => {
                            const label = ctx[0].label
                            const desc = reqMap[label]['desc']
                            return label + ": " + desc
                        }
                    }
                }
            }
        }
    });
})

const createRequirementCoverageData = () => {
    const dataSet = {}
    rawCoverageData.testcases.forEach(tc => {
        tc.passed.forEach(cov => {
            const req = dataSet[cov.id]
            if (req == null) {
                // first occurence of this requirement
                dataSet[cov.id] = {'passed': cov.covered, 'failed': 0, 'desc': cov.description}
            } else {
                // already have this one
                req['passed'] += cov.covered
            }
        })
        tc.failed.forEach(cov => {
            const req = dataSet[cov.id]
            if (req == null) {
                // first occurence of this requirement
                dataSet[cov.id] = {'passed': 0, 'failed': cov.covered, 'desc': cov.description}
            } else {
                // already have this one
                req['failed'] += cov.covered
            }
        })
    })

    const labels = Object.keys(dataSet)
    labels.sort((a, b) => {
        const bSum = dataSet[b].passed + dataSet[b].failed
        const aSum = dataSet[a].passed + dataSet[a].failed
        return bSum - aSum
    })

    const passed = []
    labels.forEach(l => passed.push(dataSet[l].passed))

    const failed = []
    labels.forEach(l => failed.push(dataSet[l].failed))

    const coverageData = {
        labels: labels,
        datasets: [{
            label: 'Passed',
            fill: false,
            data: passed,
            backgroundColor: 'rgba(153,204,51,0.5)',
            borderColor: 'rgba(153,204,51,1)',
            borderWidth: 1,
        }, {
            label: 'Failed',
            fill: false,
            data: failed,
            backgroundColor: ['rgba(255, 22, 49, 0.5)'],
            borderColor: ['rgba(255, 22, 49, 1)'],
            borderWidth: 1,
        }
        ]
    }

    return coverageData
}

const getCoveredRequirementsMap = () => {
    const dataSet = {}
    rawCoverageData.testcases.forEach(tc => {
        tc.passed.forEach(cov => {
            const req = dataSet[cov.id]
            if (req == null) {
                // first occurence of this requirement
                dataSet[cov.id] = {'desc': cov.description}
            }
        })
        tc.failed.forEach(cov => {
            const req = dataSet[cov.id]
            if (req == null) {
                // first occurence of this requirement
                dataSet[cov.id] = {'desc': cov.description}
            }
        })
    })
    return dataSet
}

const create_coverage_row = () => {
    const fluid = document.createElement('div')
    fluid.className = 'container-fluid'

    const charts_row = document.createElement('div')
    charts_row.className = 'dashboard-charts row'

    const coverage_chart = create_coverage_chart()

    charts_row.appendChild(coverage_chart)
    fluid.appendChild(charts_row)

    return fluid
}

const create_coverage_chart = () => {
    const outer = document.createElement('div')
    outer.className = 'col-lg-4 col-md-6 col-sm-9'

    const heading = document.createElement('h4')
    heading.innerHTML = "<i class='bi bi-clipboard-check'></i> Requirement Coverage"

    outer.appendChild(heading)

    const chart_container = document.createElement('div')
    chart_container.className = 'chart-container'

    const canvas = document.createElement('canvas')
    canvas.id = 'coverageChart'
    canvas.height = 200

    chart_container.appendChild(canvas)
    outer.appendChild(chart_container)
    return outer
}
