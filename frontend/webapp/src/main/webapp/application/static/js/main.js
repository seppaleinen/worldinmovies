'use strict';

/**
$(window).load(function() {
     $('#loading').hide();
});
**/

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
    }
    return "";
}


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

    function getCookie(name) {
        var value = "; " + document.cookie;
        var parts = value.split("; " + name + "=");
        if (parts.length == 2) return parts.pop().split(";").shift();
    }

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
                        var message = ''
                        $.each(data, function(key, value) {
                            message += value.name + ":" + value.year + '<br/>';
                        });
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
                //add movie button
            },
            onLoad: function(event, map) {
                var session_id = getCookie('session_id')

                $.ajax({
                    url: '/user_info',
                    type: 'POST',
                    crossDomain: false,
                    success: function(data) {
                        $.each(jQuery.parseJSON(data), function(key, value) {
                            if (value.countrySet) {
                                for (var i = 0, len = value.countrySet.length; i < len; i++) {
                                    if (map.countries[value.countrySet[i].toLowerCase()]) {
                                        map.countries[value.countrySet[i].toLowerCase()].setFill(found_color);
                                    }
                                }
                            }
                        });
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        var message = 'Call to backend failed';

                        jQuery('#popup').text(message);
                        $(this).addClass('selected');
                        $('.pop').slideFadeToggle();
                    }
                });
            },
            onRegionOver: function(event, code, region) {
                //show info
                var message = 'You hoovered "' + region + '" which has the code: ' + code.toUpperCase();
            },
            onLabelShow: function(event, label, code) {
                //show info
                //label.text("HEJHEJ");
            },
        });
    }

    renderMap();
});