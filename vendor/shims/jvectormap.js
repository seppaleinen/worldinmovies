(function() {
  function vendorModule() {
    'use strict';

    return { 'default': self['jvectormap'] };
  }

  define('jvectormap', [], vendorModule);
})();
