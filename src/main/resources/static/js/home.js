// Home page shelf: hover, focus or tap a spine to preview the book in the panel.
(function () {
  var shelf = document.getElementById('shelf');
  var panel = document.getElementById('shelfPanel');
  if (!shelf || !panel) return;

  var spines = [].slice.call(shelf.querySelectorAll('.spine'));
  var f = {};
  panel.querySelectorAll('[data-f]').forEach(function (n) { f[n.dataset.f] = n; });
  var canHover = window.matchMedia('(hover: hover)').matches;
  var calm = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function stockInfo(n) {
    if (n === 0) return ['tag out', 'Out of stock'];
    if (n <= 5) return ['tag low', 'Only ' + n + ' left'];
    return ['tag ok', 'In stock'];
  }

  function show(s) {
    if (s.classList.contains('active')) return;
    spines.forEach(function (x) { x.classList.remove('active'); x.removeAttribute('aria-current'); });
    s.classList.add('active');
    s.setAttribute('aria-current', 'true');

    var d = s.dataset, stock = parseInt(d.stock, 10) || 0, st = stockInfo(stock);
    f.title.textContent = d.title;
    f.author.textContent = 'by ' + d.author;
    f.genre.textContent = d.genre;
    f.desc.textContent = d.desc;
    f.price.textContent = d.price;
    f.stock.className = st[0];
    f.stock.textContent = st[1];
    f.bookId.value = d.id;
    f.form.hidden = stock <= 0;
    f.link.href = s.href;
  }

  function bring(s) {
    s.scrollIntoView({ inline: 'center', block: 'nearest', behavior: calm ? 'auto' : 'smooth' });
  }

  spines.forEach(function (s, i) {
    if (canHover) s.addEventListener('mouseenter', function () { show(s); });
    s.addEventListener('focus', function () { show(s); });
    // On touch screens the first tap previews; a second tap on the same spine opens the book.
    s.addEventListener('click', function (e) {
      if (!s.classList.contains('active')) { e.preventDefault(); show(s); bring(s); }
    });
    s.addEventListener('keydown', function (e) {
      var next = e.key === 'ArrowRight' ? spines[i + 1] : e.key === 'ArrowLeft' ? spines[i - 1] : null;
      if (next) { e.preventDefault(); next.focus(); bring(next); }
    });
  });
})();
