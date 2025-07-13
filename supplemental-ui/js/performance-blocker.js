// Performance optimization: Block slow external eclipse.org resources
(function() {
  'use strict';
  
  // Block eclipse.org cookie consent requests immediately
  const blockedDomains = [
    'www.eclipse.org/eclipse.org-common/themes/solstice/public/stylesheets/vendor/cookieconsent/',
    'www.eclipse.org/eclipse.org-common/themes/solstice/public/javascript/vendor/cookieconsent/'
  ];
  
  // Override fetch to block requests
  const originalFetch = window.fetch;
  window.fetch = function(url, options) {
    const urlString = url.toString();
    if (blockedDomains.some(domain => urlString.includes(domain))) {
      console.log('Blocked slow external request:', urlString);
      return Promise.reject(new Error('Blocked for performance'));
    }
    return originalFetch.apply(this, arguments);
  };
  
  // Override XMLHttpRequest
  const originalXMLHttpRequest = window.XMLHttpRequest;
  window.XMLHttpRequest = function() {
    const xhr = new originalXMLHttpRequest();
    const originalOpen = xhr.open;
    
    xhr.open = function(method, url) {
      const urlString = url.toString();
      if (blockedDomains.some(domain => urlString.includes(domain))) {
        console.log('Blocked slow XHR request:', urlString);
        return; // Don't open the request
      }
      return originalOpen.apply(this, arguments);
    };
    
    return xhr;
  };
  
  // Remove existing elements immediately if they exist
  document.addEventListener('DOMContentLoaded', function() {
    // Remove cookie consent links
    const cookieLinks = document.querySelectorAll('link[href*="eclipse.org"][href*="cookieconsent"]');
    cookieLinks.forEach(link => {
      console.log('Removing eclipse.org cookie consent CSS:', link.href);
      link.remove();
    });
    
    // Remove cookie consent scripts
    const cookieScripts = document.querySelectorAll('script[src*="eclipse.org"][src*="cookieconsent"]');
    cookieScripts.forEach(script => {
      console.log('Removing eclipse.org cookie consent JS:', script.src);
      script.remove();
    });
  });
  
  // Early removal - check immediately
  if (document.readyState === 'loading') {
    const observer = new MutationObserver(function(mutations) {
      mutations.forEach(function(mutation) {
        mutation.addedNodes.forEach(function(node) {
          if (node.nodeType === 1) { // Element node
            if (node.tagName === 'LINK' && node.href && node.href.includes('eclipse.org') && node.href.includes('cookieconsent')) {
              console.log('Blocked cookie consent CSS from loading:', node.href);
              node.remove();
            }
            if (node.tagName === 'SCRIPT' && node.src && node.src.includes('eclipse.org') && node.src.includes('cookieconsent')) {
              console.log('Blocked cookie consent JS from loading:', node.src);
              node.remove();
            }
          }
        });
      });
    });
    
    observer.observe(document.documentElement, {
      childList: true,
      subtree: true
    });
    
    // Stop observing after page load
    window.addEventListener('load', function() {
      observer.disconnect();
    });
  }
})();
