'use strict';

function ChartItem(value, color, highlight, label) {
    this.value = value;
    this.color = color;
    this.highlight = highlight;
    this.label = label;
}

$(document).ready(function() {
    function deselect(e) {
        $('.pop').slideFadeToggle(function() {
            e.removeClass('selected');
        });
    }
    $.fn.slideFadeToggle = function(easing, callback) {
        return this.animate({
            opacity: 'toggle',
            height: 'toggle'
        }, 'fast', easing, callback);
    };

    function renderChart() {
        var data = jQuery('#data').text();

        if (data.length === 0 || !data.trim()) {
            $('#imdbRatings').click();
        } else {
            var map = {};
            data = jQuery.parseJSON(data);
            $.each(jQuery.parseJSON(data), function(key, value) {
                if (value.country) {
                    var countryCode = value.country.toLowerCase();
                    var i = map[countryCode];
                    if (i != undefined && i.length > 0) {
                        i.push(value.name);
                        map[countryCode] = i;
                    } else {
                        var array = [];
                        array.push(value.name);
                        map[countryCode] = array;
                    }
                }
            });
            var data = [];
            for (var key in map) {
                if (map.hasOwnProperty(key)) {
                    data.push(new ChartItem(map[key].length, "#F7464A", "#FF5A5E", key));
                }
            }
            var ctx2 = document.getElementById("chart-area").getContext("2d");
            window.myPie = new Chart(ctx2).Pie(data, {
                animationEasing: "easeOutBounce",
                animateRotate: true,
                animateScale: false,
                customTooltips: function(tooltip) {
                    var tooltipEl = $('#chartjs-tooltip');
                    // Hide if no tooltip
                    if (!tooltip) {
                        tooltipEl.css({
                            opacity: 0
                        });
                        return;
                    }
                    // Set caret Position
                    tooltipEl.removeClass('above below');
                    tooltipEl.addClass(tooltip.yAlign);
                    // Set Text
                    tooltipEl.html(tooltip.text + " countries");
                    // Find Y Location on page
                    var top;
                    if (tooltip.yAlign == 'above') {
                        top = tooltip.y - tooltip.caretHeight - tooltip.caretPadding;
                    } else {
                        top = tooltip.y + tooltip.caretHeight + tooltip.caretPadding;
                    }
                    // Display, position, and set styles for font
                    tooltipEl.css({
                        opacity: 1,
                        left: tooltip.chart.canvas.offsetLeft + tooltip.x + 'px',
                        top: tooltip.chart.canvas.offsetTop + top + 'px',
                        fontFamily: tooltip.fontFamily,
                        fontSize: tooltip.fontSize,
                        fontStyle: tooltip.fontStyle,
                    });
                }
            });
        }
    }

    renderChart();
});