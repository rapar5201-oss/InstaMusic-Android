(function() {
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
    var style = document.createElement('style');
    style.id = '__hide_lovable_badge';
    style.textContent = '#lovable-badge { display: none !important; visibility: hidden !important; opacity: 0 !important; pointer-events: none !important; }';
    (document.head || document.documentElement).appendChild(style);
  } catch(e) {}

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
        ms.setActionHandler = function(a, h) {
          actions[a] = h;
          try { if (orig) return orig(a, h); } catch(e) {}
        };
        ms.__instaHookedActions = true;
      } catch(e) {}
    }
  }

  window.__triggerMediaAction = function(act) {
    if (actions[act]) {
      try { actions[act](); return; } catch(e) {}
    }
    audioElements.forEach(function(a) {
      try {
        if (act === 'play') a.play();
        else if (act === 'pause') a.pause();
      } catch(e) {}
    });
  };

  function getArt(meta) {
    if (!meta || !meta.artwork || !meta.artwork.length) return '';
    var item = meta.artwork[meta.artwork.length - 1];
    return item && item.src ? item.src : '';
  }

  function extractFromStorage(audio) {
    try {
      var currentId = (audio && audio.dataset && audio.dataset.id) ? audio.dataset.id : null;
      if (!currentId) {
        var demoRaw = localStorage.getItem('insta-music-demo-v1');
        if (demoRaw) {
          var demo = JSON.parse(demoRaw);
          if (demo && demo.player && demo.player.currentId) {
            currentId = demo.player.currentId;
          }
        }
      }
      var liveRaw = localStorage.getItem('insta-music-live-tracks-v1');
      if (!liveRaw) return null;
      var live = JSON.parse(liveRaw);
      var songs = (live && live.s) || [];
      var artists = (live && live.a) || [];
      var song = null;
      if (currentId) {
        song = songs.find(function(it){ return it && it.id === currentId; });
      }
      if (!song && audio && audio.src) {
        song = songs.find(function(it){ return it && (it.src === audio.src || it.src === audio.currentSrc); });
      }
      if (!song) return currentId ? { id: String(currentId), title: '', artist: '', cover: '', album: 'INSTA MUSIC' } : null;
      var artistObj = artists.find(function(it){ return it && it.id === song.artistId; });
      var artistName = artistObj ? artistObj.name : '';
      return {
        id: String(song.id || currentId || ''),
        title: song.title || '',
        artist: artistName || song.artist || '',
        cover: song.cover || song.artwork || '',
        album: song.album || 'INSTA MUSIC'
      };
    } catch(e) {
      return null;
    }
  }

  function extractFromMiniPlayer() {
    var btn = document.querySelector('button[aria-label="Open Now Playing"]');
    if (!btn) return null;
    var img = btn.querySelector('img');
    var cover = img ? (img.currentSrc || img.src || '') : '';
    var spans = btn.querySelectorAll('span');
    var texts = [];
    for (var i = 0; i < spans.length; i++) {
      var s = spans[i];
      if (s.children.length === 0) {
        var t = (s.innerText || s.textContent || '').trim();
        if (t) texts.push(t);
      }
    }
    if (texts.length >= 1) {
      return {
        title: texts[0],
        artist: texts.length > 1 ? texts[1] : '',
        cover: cover,
        album: 'INSTA MUSIC'
      };
    }
    return null;
  }

  function extractFromFullPlayer() {
    var h1 = document.querySelector('h1');
    if (!h1) return null;
    var title = (h1.innerText || h1.textContent || '').trim();
    if (!title || title.length > 100) return null;
    var artistLink = document.querySelector('a[href*="/artist/"]');
    var artist = artistLink ? (artistLink.innerText || artistLink.textContent || '').trim() : '';
    var img = document.querySelector('img.aspect-square');
    var cover = img ? (img.currentSrc || img.src || '') : '';
    return {
      title: title,
      artist: artist,
      cover: cover,
      album: 'INSTA MUSIC'
    };
  }

  function sync() {
    hookMediaSession();
    var audios = document.getElementsByTagName('audio');
    for (var i = 0; i < audios.length; i++) addAudio(audios[i]);

    var active = null;
    audioElements.forEach(function(a) {
      if (!active || (!a.paused && !a.ended)) active = a;
    });

    var playing = navigator.mediaSession
      ? (navigator.mediaSession.playbackState === 'playing' || !!(active && !active.paused && !active.ended))
      : !!(active && !active.paused && !active.ended);

    var meta = null;
    meta = extractFromStorage(active);

    if (!meta || !meta.title) {
      if (navigator.mediaSession && navigator.mediaSession.metadata) {
        var m = navigator.mediaSession.metadata;
        if (m && m.title && m.title !== 'INSTA MUSIC') {
          meta = { id: '', title: m.title, artist: m.artist || '', album: m.album || 'INSTA MUSIC', cover: getArt(m) };
        }
      }
    }

    if (!meta || !meta.title) {
      meta = extractFromMiniPlayer();
    }

    if (!meta || !meta.title) {
      meta = extractFromFullPlayer();
    }

    if (meta && meta.title && meta.title.trim()) {
      reportMeta(meta.title, meta.artist, meta.album || 'INSTA MUSIC', meta.cover || '', playing, meta.id || '');
    } else if (meta && meta.id && meta.id !== lastIdentity) {
      // A new track is selected but its metadata hasn't hydrated yet. Clear old text/art
      // immediately rather than displaying the previous track during SPA transitions.
      reportMeta('', '', 'INSTA MUSIC', '', playing, meta.id, true);
    }

    if (active) reportInfo(active, false);
  }

  // SPA route changes do not reload the WebView. Observe both history changes and
  // DOM mutations, while the polling pass catches same-document storage updates.
  try {
    ['pushState', 'replaceState'].forEach(function(name) {
      var original = history[name];
      if (typeof original !== 'function' || original.__instaWrapped) return;
      var wrapped = function() {
        var result = original.apply(this, arguments);
        scheduleSync();
        return result;
      };
      wrapped.__instaWrapped = true;
      history[name] = wrapped;
    });
    window.addEventListener('popstate', scheduleSync);
    window.addEventListener('hashchange', scheduleSync);
    window.addEventListener('pageshow', scheduleSync);
    window.addEventListener('focus', scheduleSync);
    document.addEventListener('visibilitychange', scheduleSync);
    if (window.MutationObserver && document.documentElement) {
      var observer = new MutationObserver(scheduleSync);
      observer.observe(document.documentElement, {subtree:true, childList:true, characterData:true, attributes:true, attributeFilter:['src','data-id','aria-label']});
    }
  } catch(e) {}

  sync();
  setInterval(function() {
    try { sync(); } catch(e) {}
  }, 400);
})();
