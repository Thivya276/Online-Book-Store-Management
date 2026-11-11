// Client-side checks for forms marked data-validate. The server validates again.
document.querySelectorAll('form[data-validate]').forEach(function (form) {
  var pw = form.elements.password, confirm = form.elements.confirm;
  function check() {
    if (pw && confirm) {
      pw.setCustomValidity(pw.value.length >= 6 ? '' : 'Use at least 6 characters.');
      confirm.setCustomValidity(pw.value === confirm.value ? '' : 'Passwords do not match.');
    }
  }
  form.addEventListener('input', check);
  form.addEventListener('submit', function (e) {
    check();
    if (!form.checkValidity()) { e.preventDefault(); form.reportValidity(); }
  });
});
