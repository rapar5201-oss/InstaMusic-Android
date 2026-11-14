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

  // Network quality & speed querying
  var lastNetworkPercent = -1;
  var chipIsCompact = false;
  var chipIdleTimer = null;
  var wasOffline = false;
  var onlineDismissTimer = null;

  function queryNetworkQuality() {
    var online = checkIsOnline();
    if (!online) {
      return { online: false, percent: 0, label: 'Offline', type: 'none' };
    }

    if (window.MediaBridge && typeof window.MediaBridge.getNetworkStatus === 'function') {
      try {
        var raw = window.MediaBridge.getNetworkStatus();
        if (raw) {
          var parsed = JSON.parse(raw);
          if (parsed && typeof parsed.percent === 'number') {
            return parsed;
          }
        }
      } catch(e) {}
    }

    // Web fallback using navigator.connection
    var conn = navigator.connection || navigator.mozConnection || navigator.webkitConnection;
    var percent = 75;
    var label = 'Good internet';
    var type = 'Wi-Fi';

    if (conn) {
      var downlink = conn.downlink || 5;
      if (conn.effectiveType === 'slow-2g' || conn.effectiveType === '2g') {
        percent = 22;
        label = 'Slow connection';
      } else if (conn.effectiveType === '3g' || downlink < 1.5) {
        percent = Math.max(18, Math.min(48, Math.round(downlink * 25)));
        label = 'Stable internet';
      } else if (downlink >= 8) {
        percent = Math.min(98, 85 + Math.round((downlink - 8) * 1.2));
        label = 'Fast internet';
      } else {
        percent = Math.min(84, 50 + Math.round((downlink - 1.5) * 5.2));
        label = 'Good internet';
      }
      if (conn.type) type = conn.type;
    }

    return { online: true, percent: percent, label: label, type: type };
  }

  // Spotify-style professional offline banner (Floating dark dynamic pill, NO emojis)
  function updateOfflineUI() {
    var online = checkIsOnline();
    var pill = document.getElementById('__im_offline_pill');

    // Clean up legacy status bar if present
    var oldBar = document.getElementById('__offline_status_bar');
    if (oldBar && oldBar.parentNode) oldBar.parentNode.removeChild(oldBar);

    if (!online) {
      wasOffline = true;
      if (onlineDismissTimer) {
        clearTimeout(onlineDismissTimer);
        onlineDismissTimer = null;
      }

      if (!pill) {
        pill = document.createElement('div');
        pill.id = '__im_offline_pill';
        pill.style.cssText = 'position:fixed;top:12px;left:50%;transform:translateX(-50%);max-width:calc(100vw - 28px);width:max-content;background:rgba(18,18,20,0.92);backdrop-filter:blur(20px);-webkit-backdrop-filter:blur(20px);border:1px solid rgba(255,255,255,0.14);border-radius:9999px;padding:6px 14px 6px 12px;box-shadow:0 8px 32px rgba(0,0,0,0.65),0 2px 8px rgba(0,0,0,0.4);z-index:999999;display:flex;align-items:center;gap:8px;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;letter-spacing:0.2px;user-select:none;animation:imSlideDownPill 0.35s cubic-bezier(0.16,1,0.3,1) forwards;';

        var iconSvg = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#A1A1AA" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;"><line x1="1" y1="1" x2="23" y2="23"></line><path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55"></path><path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39"></path><path d="M10.71 5.05A16 16 0 0 1 22.56 9"></path><path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88"></path><path d="M8.53 16.11a6 6 0 0 1 6.95 0"></path><line x1="12" y1="20" x2="12.01" y2="20"></line></svg>';

        pill.innerHTML = [
          iconSvg,
          '<div style="display:flex;align-items:center;gap:6px;">',
          '  <span style="color:#FFFFFF;font-weight:600;font-size:12px;">Offline Mode</span>',
          '  <span style="color:rgba(255,255,255,0.25);font-size:11px;">•</span>',
          '  <span style="color:#A1A1AA;font-weight:400;font-size:12px;">Saved Library ready</span>',
          '</div>',
          '<button id="__im_offline_btn_lib" style="background:rgba(255,255,255,0.12);color:#FFFFFF;border:1px solid rgba(255,255,255,0.2);border-radius:9999px;padding:3px 10px;font-size:11px;font-weight:500;cursor:pointer;margin-left:4px;letter-spacing:0.2px;transition:background 0.2s;">Open Library</button>'
        ].join('');

        var btn = pill.querySelector('#__im_offline_btn_lib');
        if (btn) {
          btn.onclick = function(e) {
            e.preventDefault();
            e.stopPropagation();
            navigateTo('/library');
          };
        }

        if (document.body) document.body.appendChild(pill);
        else if (document.documentElement) document.documentElement.appendChild(pill);
      }

      handleSearchOffline();
    } else {
      if (wasOffline && pill) {
        wasOffline = false;
        var checkSvg = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="flex-shrink:0;"><path d="M20 6L9 17l-5-5"></path></svg>';
        pill.innerHTML = [
          checkSvg,
          '<span style="color:#10B981;font-weight:600;font-size:12px;">Back Online</span>'
        ].join('');

        onlineDismissTimer = setTimeout(function() {
          if (pill && pill.parentNode) {
            pill.style.animation = 'imSlideUpPill 0.35s cubic-bezier(0.16,1,0.3,1) forwards';
            setTimeout(function() {
              if (pill && pill.parentNode) pill.parentNode.removeChild(pill);
            }, 360);
          }
        }, 2200);
      } else if (!wasOffline && pill && pill.parentNode) {
        pill.parentNode.removeChild(pill);
      }

      var searchNotice = document.getElementById('__offline_search_notice');
      if (searchNotice && searchNotice.parentNode) {
        searchNotice.parentNode.removeChild(searchNotice);
      }
    }
  }

  // Network Speed / Connection Quality Indicator Chip
  function updateNetworkQualityUI() {
    var q = queryNetworkQuality();
    var chip = document.getElementById('__im_network_quality_chip');

    if (!chip) {
      chip = document.createElement('div');
      chip.id = '__im_network_quality_chip';
      chip.style.cssText = 'position:fixed;bottom:82px;right:14px;background:rgba(18,18,20,0.85);backdrop-filter:blur(14px);-webkit-backdrop-filter:blur(14px);border:1px solid rgba(255,255,255,0.12);border-radius:9999px;padding:4px 10px 4px 8px;box-shadow:0 4px 18px rgba(0,0,0,0.45);z-index:99998;display:flex;align-items:center;gap:6px;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;font-size:11.5px;color:#E5E7EB;cursor:pointer;user-select:none;transition:all 0.35s cubic-bezier(0.16,1,0.3,1);';

      chip.onclick = function(e) {
        e.preventDefault();
        e.stopPropagation();
        chipIsCompact = !chipIsCompact;
        resetChipIdleTimer();
        renderChipContent(chip, q, false);
      };

      if (document.body) document.body.appendChild(chip);
      else if (document.documentElement) document.documentElement.appendChild(chip);
    }

    // Dynamic positioning adjustment if audio player bar is visible
    var playerBar = document.querySelector('[data-player-bar], .mini-player, [data-bottom-player]') ||
                    (document.querySelector('audio') && document.querySelector('div.fixed.bottom-0'));
    var targetBottom = playerBar ? '138px' : '82px';
    if (chip.style.bottom !== targetBottom) {
      chip.style.bottom = targetBottom;
    }

    var dropped = (lastNetworkPercent > 0 && (lastNetworkPercent - q.percent >= 5));
    if (dropped) {
      chipIsCompact = false; // Auto-expand when speed drops so user is informed
    }

    renderChipContent(chip, q, dropped);
    lastNetworkPercent = q.percent;
  }

  function resetChipIdleTimer() {
    if (chipIdleTimer) clearTimeout(chipIdleTimer);
    chipIdleTimer = setTimeout(function() {
      // Auto-compact after 7s of quiet stability to avoid disturbing the user
      chipIsCompact = true;
      var chip = document.getElementById('__im_network_quality_chip');
      if (chip) {
        var labelSpan = chip.querySelector('.__im_chip_label');
        if (labelSpan) {
          labelSpan.style.maxWidth = '0';
          labelSpan.style.opacity = '0';
          labelSpan.style.marginLeft = '0';
        }
      }
    }, 7000);
  }

  function renderChipContent(chip, q, isDrop) {
    var accentColor = '#10B981'; // Emerald
    if (!q.online) {
      accentColor = '#6B7280'; // Slate
    } else if (q.percent < 35) {
      accentColor = '#EF4444'; // Red
    } else if (q.percent < 60) {
      accentColor = '#F59E0B'; // Amber
    }

    if (isDrop) {
      chip.style.animation = 'none';
      void chip.offsetWidth; // trigger reflow
      chip.style.animation = 'imQualityDrop 0.65s cubic-bezier(0.16,1,0.3,1)';
      chip.style.borderColor = 'rgba(245,158,11,0.45)';
      setTimeout(function() {
        if (chip) chip.style.borderColor = 'rgba(255,255,255,0.12)';
      }, 1200);
    }

    var labelText = q.online ? q.label : 'Offline';
    var percentText = q.online ? (q.percent + '%') : 'Offline';

    var dotHtml = [
      '<div style="position:relative;width:8px;height:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">',
      '  <div style="position:absolute;width:16px;height:16px;border-radius:50%;background:' + accentColor + ';opacity:0.25;animation:imPulseAura 2.4s infinite;"></div>',
      '  <div style="width:7px;height:7px;border-radius:50%;background:' + accentColor + ';box-shadow:0 0 6px ' + accentColor + ';"></div>',
      '</div>'
    ].join('');

    var labelStyle = chipIsCompact ? 'max-width:0;opacity:0;overflow:hidden;margin-left:0;' : 'max-width:140px;opacity:1;margin-left:2px;';

    chip.innerHTML = [
      dotHtml,
      '<span class="__im_chip_val" style="font-weight:700;color:#FFFFFF;letter-spacing:0.1px;' + (isDrop ? 'animation:imNumberFlip 0.35s ease;' : '') + '">' + percentText + '</span>',
      '<span class="__im_chip_label" style="color:#A1A1AA;font-weight:400;font-size:11px;white-space:nowrap;transition:all 0.3s cubic-bezier(0.16,1,0.3,1);' + labelStyle + '">' + labelText + '</span>'
    ].join('');

    resetChipIdleTimer();
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
          searchNotice.style.cssText = 'margin:14px 16px;padding:14px 16px;background:rgba(24,24,27,0.85);backdrop-filter:blur(12px);border:1px solid rgba(255,255,255,0.1);border-radius:18px;color:#F3F4F6;font-size:13px;display:flex;flex-direction:column;gap:8px;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,sans-serif;box-shadow:0 6px 20px rgba(0,0,0,0.4);';
          searchNotice.innerHTML = [
            '<div style="display:flex;align-items:center;gap:8px;">',
            '  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#A1A1AA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line><line x1="11" y1="8" x2="11" y2="14"></line><line x1="8" y1="11" x2="14" y2="11"></line></svg>',
            '  <strong style="font-weight:600;color:#FFFFFF;font-size:13.5px;">Search requires an internet connection</strong>',
            '</div>',
            '<span style="color:#A1A1AA;font-size:12px;line-height:1.45;">You are currently offline. You can still access and play all your saved tracks, playlists, and downloads in your Library.</span>',
            '<button style="align-self:flex-start;background:rgba(255,255,255,0.12);color:#FFFFFF;border:1px solid rgba(255,255,255,0.22);border-radius:9999px;padding:6px 16px;font-size:12px;font-weight:500;cursor:pointer;margin-top:2px;letter-spacing:0.2px;transition:background 0.2s;">Open Saved Library</button>'
          ].join('');
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

  function handlePeriodicNetworkUpdates() {
    updateOfflineUI();
    updateNetworkQualityUI();
  }

  window.addEventListener('online', handlePeriodicNetworkUpdates);
  window.addEventListener('offline', handlePeriodicNetworkUpdates);
  window.addEventListener('popstate', function() {
    setTimeout(handlePeriodicNetworkUpdates, 100);
  });
  setInterval(handlePeriodicNetworkUpdates, 2800);
  setTimeout(handlePeriodicNetworkUpdates, 1000);

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
