'use strict';

/**
$(window).load(function() {
     $('#loading').hide();
});
**/

$(document).ready(function() {
    var default_color = "#8B0000";
    var found_color = "#00ff00";

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

    function renderMap() {
        jQuery('#vmap').vectorMap({
            map: 'world_en',
            hoverOpacity: 0.7,
            hoverColor: false,
            onRegionClick: function(element, code, region) {
                $.ajax({
                    url: '/findMoviesByCountry/' + code,
                    type: 'GET',
                    crossDomain: false,
                    success: function(data) {
                        //console.log(data);
                        //var message = 'You clicked "' + region + '" which has the code: ' + code.toUpperCase();
                        var message = ''
                        for(var i in data) {
                            message = message + data[i].name + ":" + data[i].year + '<br/>';
                        }

                        jQuery('#popup').html(message);
                        if ($(this).hasClass('selected')) {
                            deselect($(this));
                        } else {
                            $(this).addClass('selected');
                            $('.pop').slideFadeToggle();
                        }

                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        var message = 'Call to backend failed'

                        jQuery('#popup').text(message);
                        $(this).addClass('selected');
                        $('.pop').slideFadeToggle();
                    }
                });

                //show info
                //add movie button
            },
            onLoad: function(event, map) {
                //load data
                var data = jQuery('#data').text();

                if ((data.length === 0 || !data.trim())) {
                    $.ajax({
                        url: '/findCountries',
                        type: 'GET',
                        crossDomain: false,
                        //data: 'ID=1&Name=John&Age=10', // or $('#myform').serializeArray()
                        success: function(data) {
                            for (var i in data) {
                                var country = map.countries[data[i].code.toLowerCase()];
                                if (country != undefined) {
                                    map.countries[data[i].code.toLowerCase()].setFill(default_color);
                                }
                            }
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            var message = 'Call to backend failed';

                            jQuery('#popup').text(message);
                            $(this).addClass('selected');
                            $('.pop').slideFadeToggle();
                        }
                    });
                } else {
                    data = jQuery.parseJSON(data);
                    $.each(jQuery.parseJSON(data), function(key, value) {
                        if(value.country) {
                            var countryCode = value.country.toLowerCase()
                            if (map.countries[countryCode]) {
                                map.countries[countryCode].setFill(found_color);
                            }
                        }
                    });
                }
            },
            onRegionOver: function(event, code, region) {
                //show info
                var message = 'You hoovered "' + region + '" which has the code: ' + code.toUpperCase();

                //console.log(message);
            },
            onLabelShow: function(event, label, code) {
                //show info
                //label.text("HEJHEJ");
            },
        });
    }

    renderMap();

});