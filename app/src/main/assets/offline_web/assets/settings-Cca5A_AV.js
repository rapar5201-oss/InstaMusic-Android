import{n as _e}from"./rolldown-runtime-CbXtAM7H.js";
import{Ct as e,Dt as t,It as n,Tt as r,Z as i,et as a,n as o,q as s,rt as c,y as l,ln as _ln}from"./music-BLVe8YTA.js";
import{t as u}from"./sliders-horizontal-CrysWpKz.js";
import{s as d}from"./index-BU8D3Ji9.js";
import{t as f}from"./switch-Bsly0rYV.js";

var p=a(`refresh-cw`,[[`path`,{d:`M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8`,key:`v9h5vc`}],[`path`,{d:`M21 3v5h-5`,key:`1q7to0`}],[`path`,{d:`M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16`,key:`3uifl3`}],[`path`,{d:`M8 16H3v5`,key:`1cv678`}]]),
m=a(`vibrate`,[[`path`,{d:`m2 8 2 2-2 2 2 2-2 2`,key:`sv1b1`}],[`path`,{d:`m22 8-2 2 2 2-2 2`,key:`101i4y`}],[`rect`,{width:`8`,height:`14`,x:`8`,y:`5`,rx:`1`,key:`1oyrl4`}]]),
h=a(`volume-2`,[[`path`,{d:`M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z`,key:`uqj9uw`}],[`path`,{d:`M16 9a5 5 0 0 1 0 6`,key:`1q6k2b`}],[`path`,{d:`M19.364 18.364a9 9 0 0 0 0-12.728`,key:`ijwkga`}]]),
shieldIcon=a(`shield`,[[`path`,{d:`M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z`,key:`sh1`}],[`path`,{d:`m9 12 2 2 4-4`,key:`sh2`}]]),
infoIcon=a(`info`,[[`circle`,{cx:`12`,cy:`12`,r:`10`,key:`inf1`}],[`path`,{d:`M12 16v-4`,key:`inf2`}],[`path`,{d:`M12 8h.01`,key:`inf3`}]]),
fileTextIcon=a(`file-text`,[[`path`,{d:`M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z`,key:`ft1`}],[`path`,{d:`M14 2v4a2 2 0 0 0 2 2h4`,key:`ft2`}],[`path`,{d:`M10 9H8`,key:`ft3`}],[`path`,{d:`M16 13H8`,key:`ft4`}],[`path`,{d:`M16 17H8`,key:`ft5`}]]),
closeIcon=a(`x`,[[`path`,{d:`M18 6 6 18`,key:`cx1`}],[`path`,{d:`m6 6 12 12`,key:`cx2`}]]);

var g=n();
var _k=_e(_ln());

function _({icon:e,title:t,sub:n,right:r}){
  return (0,g.jsxs)(`div`,{
    className:`grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 px-4 py-3.5`,
    children:[
      (0,g.jsx)(`span`,{className:`grid h-9 w-9 place-items-center rounded-xl bg-surface-2 text-brand-blue`,children:e}),
      (0,g.jsxs)(`div`,{className:`min-w-0`,children:[
        (0,g.jsx)(`p`,{className:`text-sm font-semibold text-foreground`,children:t}),
        n&&(0,g.jsx)(`p`,{className:`text-xs text-muted-foreground`,children:n})
      ]}),
      r
    ]
  });
}

function v(){
  let n=c(), a=d(), v=n.settings||{};
  let [bass,setBass]=(0,_k.useState)(()=>parseFloat(localStorage.getItem(`im_eq_bass`)||`0`));
  let [vocal,setVocal]=(0,_k.useState)(()=>parseFloat(localStorage.getItem(`im_eq_vocal`)||`0`));
  let [treble,setTreble]=(0,_k.useState)(()=>parseFloat(localStorage.getItem(`im_eq_treble`)||`0`));
  let [remindersEnabled,setRemindersEnabled]=(0,_k.useState)(()=>{
    try{
      return window.Android&&typeof window.Android.getRemindersEnabled===`function`?window.Android.getRemindersEnabled():localStorage.getItem(`im_reminders_enabled`)!==`false`;
    }catch{
      return localStorage.getItem(`im_reminders_enabled`)!==`false`;
    }
  });
  let [privacyModalOpen,setPrivacyModalOpen]=(0,_k.useState)(false);
  let [licensesModalOpen,setLicensesModalOpen]=(0,_k.useState)(false);
  let [aboutModalOpen,setAboutModalOpen]=(0,_k.useState)(false);

  let applyEq=(bVal,vVal,tVal)=>{
    setBass(bVal);
    setVocal(vVal);
    setTreble(tVal);
    try{
      localStorage.setItem(`im_eq_bass`,String(bVal));
      localStorage.setItem(`im_eq_vocal`,String(vVal));
      localStorage.setItem(`im_eq_treble`,String(tVal));
      localStorage.setItem(`im_eq_settings`,JSON.stringify({bass:bVal,vocal:vVal,treble:tVal}));
      if(typeof window.__im_setEqualizer===`function`){
        window.__im_setEqualizer(bVal,vVal,tVal);
      }
    }catch(err){}
  };

  let resetEq=()=>{
    applyEq(0,0,0);
    r.success(`Equalizer reset to flat (0 dB)`);
  };

  let toggleReminders=(enabled)=>{
    setRemindersEnabled(enabled);
    try{
      localStorage.setItem(`im_reminders_enabled`,String(enabled));
      localStorage.setItem(`im_setting_reminders`,String(enabled));
      var b=window.MediaBridge||window.Android;
      if(b&&typeof b.setRemindersEnabled===`function`)b.setRemindersEnabled(enabled);
      if(b&&typeof b.showToast===`function`){
        b.showToast(enabled?`Music notifications enabled`:`Music notifications paused`);
      }else{
        r.success(enabled?`Music notifications enabled`:`Music notifications paused`);
      }
    }catch(e){}
  };

  let applySideEffect=(key,val)=>{
    try{
      var root=document.documentElement;
      if(key===`darkTheme`){
        if(val){root.classList.add(`dark`);root.classList.remove(`light`);}
        else{root.classList.remove(`dark`);root.classList.add(`light`);}
      }else if(key===`reduceMotion`){
        if(val)root.classList.add(`reduce-motion`);
        else root.classList.remove(`reduce-motion`);
      }else if(key===`highContrast`){
        if(val)root.classList.add(`high-contrast`);
        else root.classList.remove(`high-contrast`);
      }else if(key===`largeText`){
        if(val)root.classList.add(`large-text`);
        else root.classList.remove(`large-text`);
      }else if(key===`showArtists`){
        if(!val)root.classList.add(`im-hide-artists`);
        else root.classList.remove(`im-hide-artists`);
      }else if(key===`playCounts`){
        if(!val)root.classList.add(`im-hide-playcounts`);
        else root.classList.remove(`im-hide-playcounts`);
      }else if(key===`animatedArtwork`){
        if(!val)root.classList.add(`im-disable-artwork-anim`);
        else root.classList.remove(`im-disable-artwork-anim`);
      }else if(key===`haptics`||key===`hapticSeek`){
        if(val){
          var b=window.MediaBridge||window.Android;
          if(b&&typeof b.vibrate===`function`)b.vibrate(30);
          else if(navigator.vibrate)navigator.vibrate(30);
        }
      }
    }catch(err){}
  };

  let y=(key,val)=>{
    let nextSettings={...v,[key]:val};
    n.set(`settings`,nextSettings);
    try{
      localStorage.setItem(`im_setting_`+key,String(val));
      let demoState=JSON.parse(localStorage.getItem(`insta-music-demo-v1`)||`{}`);
      demoState.settings=nextSettings;
      localStorage.setItem(`insta-music-demo-v1`,JSON.stringify(demoState));
    }catch(e){}
    applySideEffect(key,val);
    if(val)e(!0,{sound:key===`sounds`||v.sounds,haptic:key===`haptics`||v.haptics});
  };

  let allSettings=[
    {title:`Tap sounds`,sub:`A soft musical note when you tap buttons`,type:`switch`,key:`sounds`},
    {title:`Vibration`,sub:`A light buzz on each tap`,type:`switch`,key:`haptics`},
    {title:`Autoplay recommendations`,sub:`Continue with songs that match the vibe`,type:`switch`,key:`autoplay`,default:true},
    {title:`Smart Shuffle`,sub:`Prioritize your recent plays and preferred artists`,type:`switch`,key:`smartShuffle`,default:true},
    {title:`Crossfade`,sub:`Blend smoothly between tracks`,type:`switch`,key:`crossfade`},
    {title:`Gapless playback`,sub:`Remove silence between tracks`,type:`switch`,key:`gapless`,default:true},
    {title:`High quality streaming`,sub:`Use the best available audio source`,type:`switch`,key:`highQuality`,default:true},
    {title:`Data saver`,sub:`Prefer lower-bandwidth audio on mobile data`,type:`switch`,key:`dataSaver`},
    {title:`Download over Wi-Fi only`,sub:`Protect your mobile data`,type:`switch`,key:`wifiDownloads`,default:true},
    {title:`Offline mode`,sub:`Prefer cached app content when available`,type:`switch`,key:`offlineMode`,default:true},
    {title:`Auto-download liked songs`,sub:`Save favorites for offline listening`,type:`switch`,key:`autoDownload`},
    {title:`Lyrics auto-scroll`,sub:`Keep lyrics aligned with playback`,type:`switch`,key:`lyricsAutoScroll`,default:true},
    {title:`Explicit content`,sub:`Include explicit tracks in search and mixes`,type:`switch`,key:`explicitContent`,default:true},
    {title:`Normalize volume`,sub:`Reduce loudness differences between tracks`,type:`switch`,key:`normalize`,default:true},
    {title:`Show artist names`,sub:`Display artist details in song lists`,type:`switch`,key:`showArtists`,default:true},
    {title:`Show play counts`,sub:`Show track popularity in lists`,type:`switch`,key:`playCounts`,default:true},
    {title:`Animated artwork`,sub:`Enable subtle motion effects on the player`,type:`switch`,key:`animatedArtwork`,default:true},
    {title:`Reduce motion`,sub:`Minimize animations and transitions`,type:`switch`,key:`reduceMotion`},
    {title:`Dark theme`,sub:`Use a darker interface`,type:`switch`,key:`darkTheme`,default:true},
    {title:`High contrast`,sub:`Increase contrast for readability`,type:`switch`,key:`highContrast`},
    {title:`Large text`,sub:`Use larger labels and controls`,type:`switch`,key:`largeText`},
    {title:`Haptic seek feedback`,sub:`Vibrate while seeking`,type:`switch`,key:`hapticSeek`},
    {title:`Show player queue`,sub:`Keep upcoming tracks visible`,type:`switch`,key:`showQueue`,default:true},
    {title:`Open player on tap`,sub:`Open full player after selecting artwork`,type:`switch`,key:`openPlayer`,default:true},
    {title:`Notifications`,sub:`Show music recommendations and reminders`,type:`switch`,key:`reminders`,default:true},
    {title:`Reminder frequency`,sub:`Choose when you want a listening reminder`,type:`select`,key:`reminderFrequency`},
    {title:`Language`,sub:`Choose the app display language`,type:`select`,key:`language`},
    {title:`Preferred streaming quality`,sub:`Choose audio quality when available`,type:`select`,key:`streamQuality`},
    {title:`Default landing page`,sub:`Choose where the app opens`,type:`select`,key:`landingPage`},
    {title:`Clear recent searches`,sub:`Remove your saved search history`,type:`action`,key:`clearSearch`},
    {title:`Clear listening history`,sub:`Remove recent tracks from this device`,type:`action`,key:`clearHistory`},
    {title:`Reset app preferences`,sub:`Return to the default experience`,type:`action`,key:`resetSettings`},
    {title:`Privacy policy`,sub:`Read how InstaMusic handles your data`,type:`link`,key:`privacy`},
    {title:`Open-source licenses`,sub:`View third-party notices and licenses`,type:`link`,key:`licenses`},
    {title:`App version`,sub:`Check for the latest available version`,type:`action`,key:`checkUpdates`},
    {title:`About InstaMusic`,sub:`Product details and contact information`,type:`link`,key:`about`}
  ];

  let renderSetting=(item)=>{
    if(item.type===`switch`){
      let checked=item.key===`reminders`?remindersEnabled:(v[item.key]??item.default??false);
      return (0,g.jsx)(_,{
        icon:(0,g.jsx)(h,{className:`h-4 w-4`}),
        title:item.title,
        sub:item.sub,
        right:(0,g.jsx)(f,{
          "aria-label":item.title,
          checked:checked,
          onCheckedChange:val=>{
            if(item.key===`reminders`)toggleReminders(val);
            else y(item.key,val);
          }
        })
      },item.key);
    }
    if(item.type===`select`){
      let options=item.key===`reminderFrequency`?[`10 minutes`,`30 minutes`,`1 hour`,`Off`]
        :item.key===`language`?[`English`,`Malayalam`,`Hindi`,`Tamil`,`Telugu`]
        :item.key===`streamQuality`?[`Automatic`,`High (320 kbps)`,`Standard (160 kbps)`,`Data Saver (96 kbps)`]
        :item.key===`landingPage`?[`Home`,`Search`,`Library`]:[];
      let current=localStorage.getItem(`im_setting_`+item.key)||options[0];
      return (0,g.jsxs)(_,{
        icon:(0,g.jsx)(h,{className:`h-4 w-4`}),
        title:item.title,
        sub:current+` • `+item.sub,
        right:(0,g.jsx)(`select`,{
          value:current,
          onChange:e=>{
            let nextVal=e.target.value;
            localStorage.setItem(`im_setting_`+item.key,nextVal);
            var b=window.MediaBridge||window.Android;
            if(item.key===`reminderFrequency`&&b&&typeof b.setReminderInterval===`function`){
              try{b.setReminderInterval(nextVal)}catch(err){}
            }
            if(item.key===`language`){
              try{
                let curP=JSON.parse(localStorage.getItem(`im_user_prefs`)||`{}`);
                curP.preferredLanguage=nextVal;
                localStorage.setItem(`im_user_prefs`,JSON.stringify(curP));
              }catch(err){}
            }
            r.success(item.title+`: `+nextVal);
          },
          className:`max-w-[130px] rounded-lg border border-input bg-surface p-2 text-xs font-semibold text-foreground`,
          children:options.map(o=>(0,g.jsx)(`option`,{value:o,children:o},o))
        })
      },item.key);
    }
    if(item.type===`action`){
      return (0,g.jsx)(l,{
        variant:`ghost`,
        className:`block h-auto w-full whitespace-normal p-0 text-left`,
        onClick:()=>{
          if(item.key===`clearSearch`){
            localStorage.removeItem(`im_recent_searches`);
            localStorage.removeItem(`insta-music-recent-searches`);
            r.success(`Recent searches cleared 🧹`);
          }else if(item.key===`clearHistory`){
            localStorage.removeItem(`im-listening-history-v1`);
            localStorage.removeItem(`im_recent_plays`);
            try{
              let demo=JSON.parse(localStorage.getItem(`insta-music-demo-v1`)||`{}`);
              demo.recent=[];
              localStorage.setItem(`insta-music-demo-v1`,JSON.stringify(demo));
            }catch(e){}
            r.success(`Listening history cleared 🧹`);
          }else if(item.key===`resetSettings`){
            localStorage.clear();
            r.success(`Settings reset to defaults`);
            setTimeout(()=>window.location.reload(),400);
          }else if(item.key===`checkUpdates`){
            var b=window.MediaBridge||window.Android;
            if(b&&typeof b.checkForUpdates===`function`){
              b.checkForUpdates();
              r.success(`Checking for updates...`);
            }else{
              r.success(`InstaMusic is up to date (v1.0.44)`);
            }
          }else if(item.key===`appVersion`){
            r.success(`InstaMusic • Version 1.0.44 (Latest Native Build)`);
          }
        },
        children:(0,g.jsx)(_,{
          icon:(0,g.jsx)(p,{className:`h-5 w-5`}),
          title:item.title,
          sub:item.sub,
          right:(0,g.jsx)(i,{className:`h-4 w-4 text-muted-foreground`})
        })
      },item.key);
    }
    if(item.type===`link`){
      return (0,g.jsx)(`button`,{
        onClick:()=>{
          if(item.key===`privacy`)setPrivacyModalOpen(true);
          else if(item.key===`licenses`)setLicensesModalOpen(true);
          else if(item.key===`about`)setAboutModalOpen(true);
        },
        className:`block w-full text-left hover:bg-accent`,
        children:(0,g.jsx)(_,{
          icon:item.key===`privacy`?(0,g.jsx)(shieldIcon,{className:`h-5 w-5`}):item.key===`licenses`?(0,g.jsx)(fileTextIcon,{className:`h-5 w-5`}):(0,g.jsx)(infoIcon,{className:`h-5 w-5`}),
          title:item.title,
          sub:item.sub,
          right:(0,g.jsx)(i,{className:`h-4 w-4 text-muted-foreground`})
        })
      },item.key);
    }
  };

  return (0,g.jsxs)(`div`,{
    className:`pb-16`,
    children:[
      (0,g.jsx)(o,{title:`Settings`}),

      // Advanced Audio Equalizer Card
      (0,g.jsxs)(`div`,{
        className:`mx-4 mt-4 rounded-3xl border border-brand-orange/30 bg-gradient-to-r from-brand-orange/10 via-surface to-transparent p-5 shadow-lg`,
        children:[
          (0,g.jsxs)(`div`,{
            className:`flex items-center justify-between`,
            children:[
              (0,g.jsx)(`p`,{className:`text-xs font-extrabold uppercase tracking-[0.18em] text-brand-orange`,children:`Advanced Audio Equalizer`}),
              (0,g.jsx)(`button`,{
                onClick:resetEq,
                className:`rounded-full bg-brand-orange/15 px-2.5 py-1 text-[10px] font-bold text-brand-orange hover:bg-brand-orange/25 active:scale-95`,
                children:`Reset (0 dB)`
              })
            ]
          }),
          (0,g.jsx)(`p`,{className:`mt-1.5 text-xs text-muted-foreground`,children:`Fine-tune Bass, Vocals, and Treble with responsive DSP hardware acceleration.`}),
          [[`Bass`,bass,v=>applyEq(v,vocal,treble)],[`Vocal`,vocal,v=>applyEq(bass,v,treble)],[`Treble`,treble,v=>applyEq(bass,vocal,v)]].map(([label,val,setter])=>(0,g.jsxs)(`div`,{
            className:`mt-4`,
            children:[
              (0,g.jsxs)(`div`,{
                className:`mb-1 flex justify-between text-xs font-semibold`,
                children:[
                  (0,g.jsx)(`span`,{className:`text-foreground`,children:label}),
                  (0,g.jsx)(`span`,{className:`text-brand-orange tabular-nums`,children:(val>0?`+`+val:val)+` dB`})
                ]
              }),
              (0,g.jsx)(`input`,{
                type:`range`,
                min:-12,
                max:12,
                step:1,
                value:val,
                onChange:e=>setter(parseFloat(e.target.value)||0),
                className:`w-full accent-brand-orange cursor-pointer`
              })
            ]
          },label))
        ]
      }),

      // Group 1: Feel & Access
      (0,g.jsx)(`p`,{className:`px-4 pt-6 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Feel & Access`}),
      (0,g.jsx)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface shadow-sm`,children:allSettings.slice(0,4).map(renderSetting)}),

      // Group 2: Playback & Recommendations
      (0,g.jsx)(`p`,{className:`px-4 pt-6 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Playback & Recommendations`}),
      (0,g.jsx)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface shadow-sm`,children:allSettings.slice(4,14).map(renderSetting)}),

      // Group 3: Library & Display
      (0,g.jsx)(`p`,{className:`px-4 pt-6 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Library & Display`}),
      (0,g.jsx)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface shadow-sm`,children:allSettings.slice(14,24).map(renderSetting)}),

      // Group 4: Notifications, Language & Data
      (0,g.jsx)(`p`,{className:`px-4 pt-6 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Notifications, Language & Data`}),
      (0,g.jsx)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface shadow-sm`,children:allSettings.slice(24).map(renderSetting)}),

      (0,g.jsxs)(`div`,{
        className:`mt-8 flex flex-col items-center gap-1.5 text-center text-xs text-muted-foreground`,
        children:[
          (0,g.jsx)(s,{className:`h-5 w-5 text-brand-orange`}),
          (0,g.jsx)(`p`,{className:`font-bold text-foreground tracking-wider`,children:`INSTA MUSIC`}),
          (0,g.jsx)(`p`,{className:`text-[10px] text-muted-foreground`,children:`Version 1.0.44 • All settings active & saved locally`})
        ]
      }),

      // Privacy Policy Modal
      privacyModalOpen&&(0,g.jsx)(`div`,{
        className:`fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/80 p-0 sm:p-4 backdrop-blur-md animate-in fade-in duration-200`,
        children:(0,g.jsxs)(`div`,{
          className:`relative w-full max-w-lg rounded-t-[2rem] sm:rounded-3xl border border-white/10 bg-[#121622] p-6 shadow-2xl max-h-[85vh] flex flex-col animate-in slide-in-from-bottom duration-300`,
          children:[
            (0,g.jsxs)(`div`,{
              className:`flex items-center justify-between pb-4 border-b border-border/60`,
              children:[
                (0,g.jsxs)(`div`,{
                  className:`flex items-center gap-2.5`,
                  children:[
                    (0,g.jsx)(`span`,{className:`text-brand-orange`,children:(0,g.jsx)(shieldIcon,{className:`h-5 w-5`})}),
                    (0,g.jsx)(`h3`,{className:`text-lg font-bold text-foreground`,children:`Privacy Policy`})
                  ]
                }),
                (0,g.jsx)(`button`,{
                  onClick:()=>setPrivacyModalOpen(false),
                  className:`grid h-8 w-8 place-items-center rounded-full bg-surface-2 text-muted-foreground hover:text-foreground`,
                  children:(0,g.jsx)(closeIcon,{className:`h-4 w-4`})
                })
              ]
            }),
            (0,g.jsxs)(`div`,{
              className:`mt-4 space-y-3 overflow-y-auto pr-1 text-xs text-muted-foreground leading-relaxed`,
              children:[
                (0,g.jsxs)(`div`,{
                  className:`rounded-xl border border-border/40 bg-surface/60 p-3.5`,
                  children:[
                    (0,g.jsx)(`p`,{className:`font-semibold text-foreground mb-1`,children:`1. No Personal Data Tracking`}),
                    (0,g.jsx)(`p`,{children:`InstaMusic does not sell your private data, collect tracking IDs, or share your listening habits with third parties.`})
                  ]
                }),
                (0,g.jsxs)(`div`,{
                  className:`rounded-xl border border-border/40 bg-surface/60 p-3.5`,
                  children:[
                    (0,g.jsx)(`p`,{className:`font-semibold text-foreground mb-1`,children:`2. Local Device Storage`}),
                    (0,g.jsx)(`p`,{children:`Playlists, liked songs, listening history, and equalizer presets are stored privately on this Android device.`})
                  ]
                }),
                (0,g.jsxs)(`div`,{
                  className:`rounded-xl border border-border/40 bg-surface/60 p-3.5`,
                  children:[
                    (0,g.jsx)(`p`,{className:`font-semibold text-foreground mb-1`,children:`3. Offline Audio Cache`}),
                    (0,g.jsx)(`p`,{children:`Tracks you explicitly save offline are stored in encrypted app storage and can be removed at any time.`})
                  ]
                }),
                (0,g.jsxs)(`div`,{
                  className:`rounded-xl border border-border/40 bg-surface/60 p-3.5`,
                  children:[
                    (0,g.jsx)(`p`,{className:`font-semibold text-foreground mb-1`,children:`4. On-Device Notifications`}),
                    (0,g.jsx)(`p`,{children:`Smart music reminders are scheduled locally on your device without sending telemetry to external push servers.`})
                  ]
                })
              ]
            }),
            (0,g.jsx)(`button`,{
              onClick:()=>setPrivacyModalOpen(false),
              className:`mt-5 w-full rounded-full bg-brand-gradient py-3 text-sm font-bold text-white shadow-glow transition active:scale-95`,
              children:`I Understand`
            })
          ]
        })
      }),

      // Open-Source Licenses Modal
      licensesModalOpen&&(0,g.jsx)(`div`,{
        className:`fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/80 p-0 sm:p-4 backdrop-blur-md animate-in fade-in duration-200`,
        children:(0,g.jsxs)(`div`,{
          className:`relative w-full max-w-lg rounded-t-[2rem] sm:rounded-3xl border border-white/10 bg-[#121622] p-6 shadow-2xl max-h-[85vh] flex flex-col animate-in slide-in-from-bottom duration-300`,
          children:[
            (0,g.jsxs)(`div`,{
              className:`flex items-center justify-between pb-4 border-b border-border/60`,
              children:[
                (0,g.jsxs)(`div`,{
                  className:`flex items-center gap-2.5`,
                  children:[
                    (0,g.jsx)(`span`,{className:`text-brand-blue`,children:(0,g.jsx)(fileTextIcon,{className:`h-5 w-5`})}),
                    (0,g.jsx)(`h3`,{className:`text-lg font-bold text-foreground`,children:`Open-Source Notices`})
                  ]
                }),
                (0,g.jsx)(`button`,{
                  onClick:()=>setLicensesModalOpen(false),
                  className:`grid h-8 w-8 place-items-center rounded-full bg-surface-2 text-muted-foreground hover:text-foreground`,
                  children:(0,g.jsx)(closeIcon,{className:`h-4 w-4`})
                })
              ]
            }),
            (0,g.jsxs)(`div`,{
              className:`mt-4 space-y-2.5 overflow-y-auto pr-1 text-xs text-muted-foreground`,
              children:[
                [[`AndroidX Media3`,`Apache 2.0 License • Google LLC`],
                 [`React & React DOM`,`MIT License • Meta Platforms, Inc.`],
                 [`TanStack Router & Query`,`MIT License • Tanner Linsley`],
                 [`Tailwind CSS`,`MIT License • Tailwind Labs, Inc.`],
                 [`Lucide Icons`,`ISC License • Lucide Contributors`],
                 [`LrcLib API`,`Public Open Music Lyrics Service`]].map(([pkg,lic])=>(0,g.jsxs)(`div`,{
                  className:`rounded-xl border border-border/40 bg-surface/60 p-3 flex justify-between items-center`,
                  children:[
                    (0,g.jsx)(`span`,{className:`font-semibold text-foreground`,children:pkg}),
                    (0,g.jsx)(`span`,{className:`text-[10px] text-muted-foreground`,children:lic})
                  ]
                },pkg))
              ]
            }),
            (0,g.jsx)(`button`,{
              onClick:()=>setLicensesModalOpen(false),
              className:`mt-5 w-full rounded-full bg-brand-gradient py-3 text-sm font-bold text-white shadow-glow transition active:scale-95`,
              children:`Close`
            })
          ]
        })
      }),

      // About InstaMusic Modal
      aboutModalOpen&&(0,g.jsx)(`div`,{
        className:`fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/80 p-0 sm:p-4 backdrop-blur-md animate-in fade-in duration-200`,
        children:(0,g.jsxs)(`div`,{
          className:`relative w-full max-w-lg rounded-t-[2rem] sm:rounded-3xl border border-white/10 bg-[#121622] p-6 shadow-2xl max-h-[85vh] flex flex-col text-center animate-in slide-in-from-bottom duration-300`,
          children:[
            (0,g.jsxs)(`div`,{
              className:`flex items-center justify-between pb-3 border-b border-border/60 text-left`,
              children:[
                (0,g.jsxs)(`div`,{
                  className:`flex items-center gap-2.5`,
                  children:[
                    (0,g.jsx)(`span`,{className:`text-brand-orange`,children:(0,g.jsx)(infoIcon,{className:`h-5 w-5`})}),
                    (0,g.jsx)(`h3`,{className:`text-lg font-bold text-foreground`,children:`About InstaMusic`})
                  ]
                }),
                (0,g.jsx)(`button`,{
                  onClick:()=>setAboutModalOpen(false),
                  className:`grid h-8 w-8 place-items-center rounded-full bg-surface-2 text-muted-foreground hover:text-foreground`,
                  children:(0,g.jsx)(closeIcon,{className:`h-4 w-4`})
                })
              ]
            }),
            (0,g.jsxs)(`div`,{
              className:`mt-5 flex flex-col items-center gap-2`,
              children:[
                (0,g.jsx)(s,{className:`h-12 w-12 text-brand-orange animate-bounce`}),
                (0,g.jsx)(`h4`,{className:`text-xl font-extrabold text-foreground tracking-wide`,children:`InstaMusic Android`}),
                (0,g.jsx)(`span`,{className:`rounded-full bg-brand-orange/20 border border-brand-orange/40 px-3 py-1 text-xs font-bold text-brand-orange`,children:`v1.0.44 Native Edition`}),
                (0,g.jsx)(`p`,{className:`mt-2 text-xs text-muted-foreground max-w-xs`,children:`Your music, your mood. Experience offline caching, real synchronized lyrics, 3-band equalizer, and Android Auto support.`}),
                (0,g.jsxs)(`div`,{
                  className:`mt-4 w-full rounded-xl border border-border/40 bg-surface/60 p-3 text-xs text-left space-y-1.5`,
                  children:[
                    (0,g.jsx)(`p`,{children:`🎵 Audio Engine: Hardware-accelerated Web Audio`}),
                    (0,g.jsx)(`p`,{children:`🚗 Car Integration: Android Auto MediaLibraryService`}),
                    (0,g.jsx)(`p`,{children:`✉️ Contact: support@instamusic.app`})
                  ]
                })
              ]
            }),
            (0,g.jsx)(`button`,{
              onClick:()=>setAboutModalOpen(false),
              className:`mt-5 w-full rounded-full bg-brand-gradient py-3 text-sm font-bold text-white shadow-glow transition active:scale-95`,
              children:`Awesome`
            })
          ]
        })
      })
    ]
  });
}

export{v as component};
