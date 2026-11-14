(function() {
  // Safe MediaMetadata mock
  if (typeof window.MediaMetadata === 'undefined') {
    window.MediaMetadata = function(init) {
      init = init || {};
      this.title = init.title || '';
      this.artist = init.artist || '';
      this.album = init.album || '';
      this.artwork = init.artwork || [];
    };
  }

  if (!navigator.mediaSession) {
    try { navigator.mediaSession = {}; } catch(e) {}
  }
  if (navigator.mediaSession) {
    try {
      var _meta = navigator.mediaSession.metadata || null;
      Object.defineProperty(navigator.mediaSession, 'metadata', {
        configurable: true,
        enumerable: true,
        get: function() { return _meta; },
        set: function(val) {
          _meta = val;
          if (val) {
            var t = (val.title || '').trim();
            var ar = (val.artist || '').trim();
            var al = val.album || 'INSTA MUSIC';
            var art = '';
            if (val.artwork && val.artwork.length) {
              var last = val.artwork[val.artwork.length - 1];
              art = (last && last.src) ? last.src : '';
            }
            if (t || ar) {
              try {
                if (window.MediaBridge && window.MediaBridge.updateMetadata) {
                  window.MediaBridge.updateMetadata(t || 'INSTA MUSIC', ar || 'Streaming', al, art, true);
                }
              } catch(e) {}
            }
          }
        }
      });
    } catch(e) {}
  }

  if (window.__instaMusicMediaHooked) return;
  window.__instaMusicMediaHooked = true;

  // Hide Lovable badge inside Android app
  try {
    var style = document.getElementById('__hide_lovable_badge') || document.createElement('style');
    style.id = '__hide_lovable_badge';
    style.textContent = '#lovable-badge, [data-lovable-badge], a[href*="lovable.dev"], .lovable-badge, button[aria-label*="Lovable" i], a[aria-label*="Lovable" i] { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; transform: scale(0) !important; width: 0 !important; height: 0 !important; overflow: hidden !important; }';
    if (!style.parentNode) (document.head || document.documentElement).appendChild(style);
  } catch(e) {}

  // Helper to determine if app is online
  function checkIsOnline() {
    if (window.MediaBridge && typeof window.MediaBridge.isOnline === 'function') {
      try {
        return window.MediaBridge.isOnline();
      } catch(e) {}
    }
    return navigator.onLine;
  }

  // Offline status notification and handling
  function updateOfflineUI() {
    var online = checkIsOnline();
    var bar = document.getElementById('__offline_status_bar');

    if (!online) {
      if (!bar) {
        bar = document.createElement('div');
        bar.id = '__offline_status_bar';
        bar.style.cssText = 'position:fixed;top:0;left:0;right:0;height:28px;background:linear-gradient(90deg,#ea580c,#f97316);color:#ffffff;font-size:12px;font-weight:700;display:flex;align-items:center;justify-content:space-between;padding:0 12px;z-index:999999;box-shadow:0 2px 10px rgba(0,0,0,0.5);letter-spacing:0.3px;font-family:system-ui,-apple-system,sans-serif;';
        
        var leftSpan = document.createElement('span');
        leftSpan.textContent = '⚡ Offline Mode • Saved Library Ready';
        bar.appendChild(leftSpan);

        var libBtn = document.createElement('button');
        libBtn.textContent = 'Go to Library';
        libBtn.style.cssText = 'background:rgba(0,0,0,0.3);border:none;border-radius:12px;padding:2px 8px;color:#fff;font-size:11px;font-weight:700;cursor:pointer;';
        libBtn.onclick = function(e) {
          e.preventDefault();
          e.stopPropagation();
          navigateTo('/library');
        };
        bar.appendChild(libBtn);

        if (document.body) document.body.appendChild(bar);
        else if (document.documentElement) document.documentElement.appendChild(bar);
      }

      // Handle search view when offline
      handleSearchOffline();
    } else {
      if (bar && bar.parentNode) {
        bar.parentNode.removeChild(bar);
      }
      var searchNotice = document.getElementById('__offline_search_notice');
      if (searchNotice && searchNotice.parentNode) {
        searchNotice.parentNode.removeChild(searchNotice);
      }
    }
  }

  function navigateTo(path) {
    if (window.location.pathname === path) return;
    try {
      window.history.pushState({}, '', path);
      window.dispatchEvent(new PopStateEvent('popstate'));
    } catch(e) {
      window.location.href = path;
    }
  }

  function handleSearchOffline() {
    if (window.location.pathname.startsWith('/search')) {
      var searchNotice = document.getElementById('__offline_search_notice');
      if (!searchNotice) {
        var form = document.querySelector('form[role="search"]') || document.querySelector('input#q');
        if (form) {
          searchNotice = document.createElement('div');
          searchNotice.id = '__offline_search_notice';
          searchNotice.style.cssText = 'margin:12px 16px;padding:12px 16px;background:rgba(234,88,12,0.15);border:1px solid rgba(234,88,12,0.4);border-radius:16px;color:#f97316;font-size:13px;display:flex;flex-direction:column;gap:8px;font-family:system-ui,-apple-system,sans-serif;';
          searchNotice.innerHTML = '<strong>Search requires internet connection</strong><span>You are currently offline. You can still access and play all your saved songs, playlists, and favorites in your Library.</span><button style="align-self:flex-start;background:#ea580c;color:#fff;border:none;border-radius:20px;padding:6px 14px;font-size:12px;font-weight:600;cursor:pointer;">Open Saved Library</button>';
          var btn = searchNotice.querySelector('button');
          if (btn) {
            btn.onclick = function(e) {
              e.preventDefault();
              navigateTo('/library');
            };
          }
          if (form.parentNode) {
            form.parentNode.insertBefore(searchNotice, form.nextSibling);
          }
        }
      }
    }
  }

  window.addEventListener('online', updateOfflineUI);
  window.addEventListener('offline', updateOfflineUI);
  window.addEventListener('popstate', function() {
    setTimeout(updateOfflineUI, 100);
  });
  setInterval(updateOfflineUI, 3000);
  setTimeout(updateOfflineUI, 1200);

  var lastTitle = '', lastArtist = '', lastAlbum = '', lastArt = '', lastIdentity = '';
  var lastPlaying = null, lastPositionSecond = -1, lastDurationSecond = -1;
  var syncTimer = 0;
  var audioElements = new Set();

  function bridge() { return window.MediaBridge || null; }

  function reportMeta(title, artist, album, art, playing, identity, force) {
    var b = bridge();
    if (!b || !b.updateMetadata) return;
    var t = (title || '').trim();
    var ar = (artist || '').trim();
    var al = (album || '').trim() || 'INSTA MUSIC';
    var image = (art || '').trim();
    var id = identity || '';
    var changed = t !== lastTitle || ar !== lastArtist || al !== lastAlbum || image !== lastArt || id !== lastIdentity;
    if (!changed && !force) return;
    lastTitle = t; lastArtist = ar; lastAlbum = al; lastArt = image; lastIdentity = id;
    try {
      b.updateMetadata(t || 'INSTA MUSIC', ar || 'Streaming', al, image, !!playing);
    } catch(e) {}
  }

  function scheduleSync() {
    if (syncTimer) return;
    syncTimer = window.setTimeout(function() {
      syncTimer = 0;
      try { sync(); } catch(e) {}
    }, 80);
  }

  function reportInfo(audio, force) {
    var b = bridge();
    if (!b || !b.updatePlaybackInfo) return;
    var pos = audio && Number.isFinite(audio.currentTime) ? audio.currentTime : 0;
    var dur = audio && Number.isFinite(audio.duration) ? audio.duration : 0;
    var playing = !!(audio && !audio.paused && !audio.ended);
    var ps = Math.floor(pos), ds = Math.floor(dur);
    if (force || playing !== lastPlaying || ps !== lastPositionSecond || ds !== lastDurationSecond) {
      lastPlaying = playing; lastPositionSecond = ps; lastDurationSecond = ds;
      try { b.updatePlaybackInfo(pos, dur, playing); } catch(e) {}
    }
  }

  function addAudio(a) {
    if (!a || audioElements.has(a)) return;
    audioElements.add(a);
    ['play','playing','pause','ended','timeupdate','durationchange','loadedmetadata'].forEach(function(ev) {
      a.addEventListener(ev, function() {
        reportInfo(a, ev === 'play' || ev === 'playing' || ev === 'pause' || ev === 'ended' || ev === 'durationchange');
        sync();
      });
    });

    a.addEventListener('error', function() {
      if (!checkIsOnline()) {
        try {
          var b = bridge();
          if (b && b.showToast) {
            b.showToast('Song is not saved offline. Open Library to listen to your saved songs!');
          }
        } catch(e) {}
      }
    });
  }

  var OrigAudio = window.Audio;
  if (OrigAudio) {
    window.Audio = function(src) {
      var a = new OrigAudio(src);
      addAudio(a);
      return a;
    };
    window.Audio.prototype = OrigAudio.prototype;
  }
  if (window.HTMLAudioElement && window.HTMLAudioElement.prototype) {
    var proto = window.HTMLAudioElement.prototype, origPlay = proto.play, origPause = proto.pause;
    proto.play = function() {
      addAudio(this);
      var r = origPlay.apply(this, arguments);
      reportInfo(this, true);
      sync();
      return r;
    };
    proto.pause = function() {
      addAudio(this);
      var r = origPause.apply(this, arguments);
      reportInfo(this, true);
      sync();
      return r;
    };
  }

  var actions = {};
  function hookMediaSession() {
    var ms = navigator.mediaSession;
    if (!ms) return;
    if (!ms.__instaHookedActions) {
      try {
        var orig = ms.setActionHandler ? ms.setActionHandler.bind(ms) : null;
        if (orig) {
          ms.setActionHandler = function(action, handler) {
            if (action) actions[action] = handler;
            return orig(action, handler);
          };
          ms.__instaHookedActions = true;
        }
      } catch(e) {}
    }
  }
  hookMediaSession();

  window.__triggerMediaAction = function(action) {
    hookMediaSession();
    var fn = actions[action];
    if (typeof fn === 'function') {
      try { fn({ action: action }); return; } catch(e) {}
    }
    var audios = Array.from(audioElements).filter(function(a) { return a && !a.ended; });
    var playing = audios.find(function(a) { return !a.paused; });
    var target = playing || audios[0] || document.querySelector('audio');
    if (!target) return;
    try {
      if (action === 'play') target.play();
      else if (action === 'pause') target.pause();
      else if (action === 'stop') { target.pause(); target.currentTime = 0; }
    } catch(e) {}
  };

  function findPlayingAudio() {
    for (var a of audioElements) {
      if (a && !a.paused && !a.ended) return a;
    }
    var domAudios = document.querySelectorAll('audio');
    for (var i = 0; i < domAudios.length; i++) {
      var d = domAudios[i];
      addAudio(d);
      if (!d.paused && !d.ended) return d;
    }
    for (var b of audioElements) {
      if (b && !b.ended) return b;
    }
    return domAudios[0] || null;
  }

  function getActiveTrackFromDom() {
    try {
      var player = document.querySelector('[data-mini-player], [data-now-playing], footer, [class*="now-playing"], [class*="player"]');
      if (player) {
        var titleEl = player.querySelector('h1, h2, h3, h4, [class*="title"], [class*="song"]');
        var artistEl = player.querySelector('p, span, [class*="artist"], [class*="creator"]');
        var imgEl = player.querySelector('img');
        var t = titleEl ? (titleEl.textContent || '').trim() : '';
        var ar = artistEl ? (artistEl.textContent || '').trim() : '';
        var img = imgEl ? (imgEl.src || '') : '';
        if (t) return { title: t, artist: ar || 'InstaMusic', album: 'INSTA MUSIC', artwork: img };
      }
    } catch(e) {}
    return null;
  }

  function getActiveTrackFromStore() {
    try {
      var keys = ['insta-music-demo-v1', 'insta-music-live-tracks-v1', 'instamusic_state', 'music-store'];
      for (var k of keys) {
        var raw = localStorage.getItem(k);
        if (raw) {
          var data = JSON.parse(raw);
          var activeId = data.activeId || data.currentSongId || (data.queue && data.queue[data.queueIndex]);
          var songs = data.songs || data.tracks || [];
          if (activeId && Array.isArray(songs)) {
            var found = songs.find(function(s) { return s.id === activeId; });
            if (found) {
              return {
                title: found.title || found.name || '',
                artist: found.artist || found.creator || '',
                album: found.album || 'INSTA MUSIC',
                artwork: found.artwork || found.cover || found.image || ''
              };
            }
          }
        }
      }
    } catch(e) {}
    return null;
  }

  function sync() {
    var audio = findPlayingAudio();
    var isPlaying = !!(audio && !audio.paused && !audio.ended);
    reportInfo(audio, false);

    var ms = navigator.mediaSession;
    var meta = ms && ms.metadata;
    if (meta && (meta.title || meta.artist)) {
      var art = '';
      if (meta.artwork && meta.artwork.length) {
        var last = meta.artwork[meta.artwork.length - 1];
        art = (last && last.src) ? last.src : '';
      }
      reportMeta(meta.title, meta.artist, meta.album || 'INSTA MUSIC', art, isPlaying, 'ms:' + meta.title + ':' + meta.artist);
      return;
    }

    var fromDom = getActiveTrackFromDom();
    if (fromDom && fromDom.title) {
      reportMeta(fromDom.title, fromDom.artist, fromDom.album, fromDom.artwork, isPlaying, 'dom:' + fromDom.title);
      return;
    }

    var fromStore = getActiveTrackFromStore();
    if (fromStore && fromStore.title) {
      reportMeta(fromStore.title, fromStore.artist, fromStore.album, fromStore.artwork, isPlaying, 'store:' + fromStore.title);
      return;
    }

    if (isPlaying) {
      reportMeta('INSTA MUSIC', 'Streaming', 'INSTA MUSIC', '', true, 'fallback', false);
    }
  }

  var observer = new MutationObserver(function() { scheduleSync(); });
  if (document.body) observer.observe(document.body, { childList: true, subtree: true, attributes: false });
  else document.addEventListener('DOMContentLoaded', function() {
    if (document.body) observer.observe(document.body, { childList: true, subtree: true, attributes: false });
  });

  setInterval(sync, 1000);
})();
