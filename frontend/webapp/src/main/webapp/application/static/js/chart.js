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
            $.ajax({
                url: '/findCountries',
                type: 'GET',
                crossDomain: false,
                success: function(result) {
                    data = [];
                    $.each(result, function(key, value) {
                        data.push(new ChartItem(1, "#F7464A", "#FF5A5E", value.name));;
                    });
                    var ctx2 = document.getElementById("chart-area").getContext("2d");
                    window.myPie = new Chart(ctx2).Pie(data);
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    var message = 'Call to backend failed ';

                    jQuery('#popup').text(message);
                    $(this).addClass('selected');
                    $('.pop').slideFadeToggle();
                }
            });
        } else {
                data = jQuery.parseJSON(data);
            $.each(jQuery.parseJSON(data), function(key, value) {
                if (value.country) {
                    var countryCode = value.country.toLowerCase()
                    if (map.countries[countryCode]) {
                        map.countries[countryCode].setFill(found_color);
                    }
                }
            });
        }
    }

    renderChart();
});