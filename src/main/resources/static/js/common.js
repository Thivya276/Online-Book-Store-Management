// Ask before destructive actions: <form data-confirm="Are you sure?">
document.addEventListener('submit', function (e) {
  var msg = e.target.dataset && e.target.dataset.confirm;
  if (msg && !window.confirm(msg)) e.preventDefault();
});
// Fade out success and error notices after a few seconds.
document.querySelectorAll('.flash').forEach(function (f) { setTimeout(function () { f.remove(); }, 7000); });

// Add the CSRF token to every POST form.
(function () {
  var m = document.querySelector('meta[name="csrf"]');
  if (!m) return;
  document.querySelectorAll('form[method="post"]').forEach(function (f) {
    if (f.querySelector('input[name="_csrf"]')) return;
    var i = document.createElement('input');
    i.type = 'hidden'; i.name = '_csrf'; i.value = m.content;
    f.appendChild(i);
  });
})();
