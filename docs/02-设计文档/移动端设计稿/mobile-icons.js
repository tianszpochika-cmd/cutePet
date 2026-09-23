/* Design-preview icon set. Every icon uses the same 24px, round-cap stroke language. */
(() => {
  const paths = {
    home: '<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V10Z"/><path d="M9 21v-7h6v7"/>',
    news: '<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 7h8M8 11h8M8 15h5"/>',
    compass: '<circle cx="12" cy="12" r="9"/><path d="m15.7 8.3-2.2 5.2-5.2 2.2 2.2-5.2 5.2-2.2Z"/>',
    user: '<circle cx="12" cy="8" r="3.5"/><path d="M5 20a7 7 0 0 1 14 0"/>',
    paw: '<circle cx="12" cy="17" r="3.2"/><circle cx="5.8" cy="11.3" r="1.4"/><circle cx="9.7" cy="7.9" r="1.4"/><circle cx="14.3" cy="7.9" r="1.4"/><circle cx="18.2" cy="11.3" r="1.4"/>',
    bell: '<path d="M18 9a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9ZM10 21h4"/>',
    calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18M8 14h3M8 17h6"/>',
    check: '<path d="m4 12 5 5L20 6"/>',
    plus: '<path d="M12 4v16M4 12h16"/>',
    arrowRight: '<path d="M4 12h16m-7-7 7 7-7 7"/>',
    arrowLeft: '<path d="M20 12H4m7-7-7 7 7 7"/>',
    chevronRight: '<path d="m9 5 7 7-7 7"/>',
    chevronLeft: '<path d="m15 5-7 7 7 7"/>',
    close: '<path d="M5 5l14 14M19 5 5 19"/>',
    users: '<circle cx="9" cy="8" r="3"/><path d="M2.5 20a6.5 6.5 0 0 1 13 0"/><path d="M17 11a3 3 0 0 0 0-6M18 15a5 5 0 0 1 3.5 5"/>',
    shield: '<path d="M12 2 4 5v6c0 5.1 3.1 8.5 8 11 4.9-2.5 8-5.9 8-11V5l-8-3Z"/><path d="m8.5 12 2.4 2.3 4.7-5"/>',
    lock: '<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/>',
    mapPin: '<path d="M19 10c0 5-7 12-7 12S5 15 5 10a7 7 0 1 1 14 0Z"/><circle cx="12" cy="10" r="2.3"/>',
    search: '<circle cx="10.7" cy="10.7" r="6.7"/><path d="m16 16 5 5"/>',
    heart: '<path d="M20.5 8.4c0 4.7-8.5 10.9-8.5 10.9S3.5 13.1 3.5 8.4a4.6 4.6 0 0 1 8.5-2.3 4.6 4.6 0 0 1 8.5 2.3Z"/>',
    bag: '<path d="M4 8h16l-1.2 13H5.2L4 8ZM9 9V6a3 3 0 0 1 6 0v3"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 6v6l4 2"/>',
    info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v6M12 7.5h.01"/>',
    alert: '<path d="m12 3 10 18H2L12 3Z"/><path d="M12 9v5M12 17h.01"/>',
    edit: '<path d="M14 5 19 10M4 20l4.5-.8L20 7.7a2 2 0 0 0-3-3L5.5 16.2 4 20Z"/>',
    camera: '<path d="M3 8h4l2-3h6l2 3h4v12H3V8Z"/><circle cx="12" cy="14" r="3"/>',
    sparkles: '<path d="m12 2 1.8 6.2L20 10l-6.2 1.8L12 18l-1.8-6.2L4 10l6.2-1.8L12 2ZM19 17l.7 2.3L22 20l-2.3.7L19 23l-.7-2.3L16 20l2.3-.7L19 17Z"/>',
    more: '<circle cx="5" cy="12" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/>',
    refresh: '<path d="M20 7a8 8 0 0 0-13-2L4 8m0-5v5h5M4 17a8 8 0 0 0 13 2l3-3m0 5v-5h-5"/>',
    image: '<rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="8" cy="9" r="1.5"/><path d="m3 17 6-5 4 3 3-2 5 4"/>',
    list: '<path d="M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01"/>',
    weight: '<path d="M4 10h16l1 11H3l1-11Z"/><path d="M7 10a5 5 0 0 1 10 0M12 10l2-2"/>',
    share: '<circle cx="18" cy="5" r="2"/><circle cx="5" cy="12" r="2"/><circle cx="18" cy="19" r="2"/><path d="m7 11 9-5M7 13l9 5"/>',
    medicine: '<rect x="5" y="3" width="14" height="18" rx="3"/><path d="M5 12h14M12 5v5"/>',
    settings: '<circle cx="12" cy="12" r="3"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4m11.4 11.4 1.4 1.4M2 12h2m16 0h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>'
  };
  const pawBrand = '<svg viewBox="0 0 32 32" fill="none" aria-hidden="true"><path d="M16 24.8c-3.4 0-4.5 2.2-7.1 1.1-2.4-1-2.8-4.1-.5-6.1 2.4-2.2 3.7-6.6 7.6-6.6s5.2 4.4 7.6 6.6c2.3 2 .8 5.1-1.5 6.1-2.6 1.1-3.8-1.1-6.8-1.1Z" fill="currentColor"/><ellipse cx="7.2" cy="10.4" rx="2.4" ry="3.5" transform="rotate(-19 7.2 10.4)" fill="currentColor"/><ellipse cx="13" cy="6.6" rx="2.4" ry="3.5" transform="rotate(-7 13 6.6)" fill="currentColor"/><ellipse cx="19.4" cy="6.6" rx="2.4" ry="3.5" transform="rotate(8 19.4 6.6)" fill="currentColor"/><ellipse cx="25" cy="10.4" rx="2.4" ry="3.5" transform="rotate(19 25 10.4)" fill="currentColor"/></svg>';
  function renderMobileIcons(root = document) {
    root.querySelectorAll('[data-icon]').forEach((node) => {
      const name = node.getAttribute('data-icon');
      if (name === 'pawBrand') node.innerHTML = pawBrand;
      else if (Object.prototype.hasOwnProperty.call(paths, name)) {
        node.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' + paths[name] + '</svg>';
      }
    });
  }
  window.renderMobileIcons = renderMobileIcons;
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', () => renderMobileIcons());
  else renderMobileIcons();
})();
