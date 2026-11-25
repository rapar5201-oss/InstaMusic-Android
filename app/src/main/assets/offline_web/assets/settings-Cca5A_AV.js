import{Ct as e,Dt as t,It as n,Tt as r,Z as i,et as a,n as o,q as s,rt as c,y as l}from"./music-BLVe8YTA.js";import{t as u}from"./sliders-horizontal-CrysWpKz.js";import{s as d}from"./index-BU8D3Ji9.js";import{t as f}from"./switch-Bsly0rYV.js";
var p=a(`refresh-cw`,[[`path`,{d:`M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8`,key:`v9h5vc`}],[`path`,{d:`M21 3v5h-5`,key:`1q7to0`}],[`path`,{d:`M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16`,key:`3uifl3`}],[`path`,{d:`M8 16H3v5`,key:`1cv678`}]]),m=a(`vibrate`,[[`path`,{d:`m2 8 2 2-2 2 2 2-2 2`,key:`sv1b1`}],[`path`,{d:`m22 8-2 2 2 2-2 2`,key:`101i4y`}],[`rect`,{width:`8`,height:`14`,x:`8`,y:`5`,rx:`1`,key:`1oyrl4`}]]),h=a(`volume-2`,[[`path`,{d:`M11 4.702a.705.705 0 0 0-1.203-.498L6.413 7.587A1.4 1.4 0 0 1 5.416 8H3a1 1 0 0 0-1 1v6a1 1 0 0 0 1 1h2.416a1.4 1.4 0 0 1 .997.413l3.383 3.384A.705.705 0 0 0 11 19.298z`,key:`uqj9uw`}],[`path`,{d:`M16 9a5 5 0 0 1 0 6`,key:`1q6k2b`}],[`path`,{d:`M19.364 18.364a9 9 0 0 0 0-12.728`,key:`ijwkga`}]]),g=n();function _({icon:e,title:t,sub:n,right:r}){return(0,g.jsxs)(`div`,{className:`grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 px-4 py-3.5`,children:[(0,g.jsx)(`span`,{className:`grid h-9 w-9 place-items-center rounded-xl bg-surface-2 text-brand-blue`,children:e}),(0,g.jsxs)(`div`,{className:`min-w-0`,children:[(0,g.jsx)(`p`,{className:`text-sm font-semibold`,children:t}),n&&(0,g.jsx)(`p`,{className:`text-xs text-muted-foreground`,children:n})]}),r]})}
function v(){
  let n=c(),a=d(),v=n.settings,y=(t,r)=>{n.set(`settings`,{...v,[t]:r}),r&&e(!0,{sound:t===`sounds`,haptic:t===`haptics`})};
  let [bass,setBass]=(0,g.useState)(()=>parseFloat(localStorage.getItem('im_eq_bass')||'0'));
  let [vocal,setVocal]=(0,g.useState)(()=>parseFloat(localStorage.getItem('im_eq_vocal')||'0'));
  let [treble,setTreble]=(0,g.useState)(()=>parseFloat(localStorage.getItem('im_eq_treble')||'0'));
  let [remindersEnabled,setRemindersEnabled]=(0,g.useState)(()=>{try{return window.Android&&typeof window.Android.getRemindersEnabled==='function'?window.Android.getRemindersEnabled():localStorage.getItem('im_reminders_enabled')!=='false'}catch{return localStorage.getItem('im_reminders_enabled')!=='false'}});
  let [expanded,setExpanded]=(0,g.useState)('');
  let [settingsCount,setSettingsCount]=(0,g.useState)(35);
  let [privacyModalOpen,setPrivacyModalOpen]=(0,g.useState)(false);

  let setEq=(type,val)=>{
    let valNum=parseFloat(val)||0;
    if(type==='bass'){setBass(valNum);localStorage.setItem('im_eq_bass',valNum)}
    if(type==='vocal'){setVocal(valNum);localStorage.setItem('im_eq_vocal',valNum)}
    if(type==='treble'){setTreble(valNum);localStorage.setItem('im_eq_treble',valNum)}
    if(window.__im_setEqualizer)window.__im_setEqualizer(bass,vocal,treble);
  };

  let toggleReminders=(enabled)=>{
    setRemindersEnabled(enabled);
    localStorage.setItem('im_reminders_enabled',String(enabled));
    var b=window.MediaBridge||window.Android;
    try{if(b&&typeof b.setRemindersEnabled==='function')b.setRemindersEnabled(enabled)}catch(e){}
  };

  let allSettings=[
    {title:'Tap sounds',sub:'A soft musical note when you tap buttons',type:'switch',key:'sounds'},
    {title:'Vibration',sub:'A light buzz on each tap',type:'switch',key:'haptics'},
    {title:'Autoplay recommendations',sub:'Continue with songs that match the vibe',type:'switch',key:'autoplay',default:true},
    {title:'Smart Shuffle',sub:'Prioritize your recent plays and preferred artists',type:'switch',key:'smartShuffle',default:true},
    {title:'Crossfade',sub:'Blend smoothly between tracks',type:'switch',key:'crossfade'},
    {title:'Gapless playback',sub:'Remove silence between tracks',type:'switch',key:'gapless',default:true},
    {title:'High quality streaming',sub:'Use the best available audio source',type:'switch',key:'highQuality',default:true},
    {title:'Data saver',sub:'Prefer lower-bandwidth audio on mobile data',type:'switch',key:'dataSaver'},
    {title:'Download over Wi-Fi only',sub:'Protect your mobile data',type:'switch',key:'wifiDownloads',default:true},
    {title:'Offline mode',sub:'Prefer cached app content when available',type:'switch',key:'offlineMode',default:true},
    {title:'Auto-download liked songs',sub:'Save favorites for offline listening',type:'switch',key:'autoDownload'},
    {title:'Lyrics auto-scroll',sub:'Keep lyrics aligned with playback',type:'switch',key:'lyricsAutoScroll',default:true},
    {title:'Explicit content',sub:'Include explicit tracks in search and mixes',type:'switch',key:'explicitContent',default:true},
    {title:'Normalize volume',sub:'Reduce loudness differences between tracks',type:'switch',key:'normalize',default:true},
    {title:'Show artist names',sub:'Display artist details in song lists',type:'switch',key:'showArtists',default:true},
    {title:'Show play counts',sub:'Show track popularity in lists',type:'switch',key:'playCounts',default:true},
    {title:'Animated artwork',sub:'Enable subtle motion effects on the player',type:'switch',key:'animatedArtwork',default:true},
    {title:'Reduce motion',sub:'Minimize animations and transitions',type:'switch',key:'reduceMotion'},
    {title:'Dark theme',sub:'Use a darker interface',type:'switch',key:'darkTheme',default:true},
    {title:'High contrast',sub:'Increase contrast for readability',type:'switch',key:'highContrast'},
    {title:'Large text',sub:'Use larger labels and controls',type:'switch',key:'largeText'},
    {title:'Haptic seek feedback',sub:'Vibrate while seeking',type:'switch',key:'hapticSeek'},
    {title:'Show player queue',sub:'Keep upcoming tracks visible',type:'switch',key:'showQueue',default:true},
    {title:'Open player on tap',sub:'Open full player after selecting artwork',type:'switch',key:'openPlayer',default:true},
    {title:'Notifications',sub:'Show music recommendations and reminders',type:'switch',key:'reminders',default:true},
    {title:'Reminder frequency',sub:'Choose when you want a listening reminder',type:'select',key:'reminderFrequency'},
    {title:'Language',sub:'Choose the app display language',type:'select',key:'language'},
    {title:'Preferred streaming quality',sub:'Choose audio quality when available',type:'select',key:'streamQuality'},
    {title:'Default landing page',sub:'Choose where the app opens',type:'select',key:'landingPage'},
    {title:'Clear recent searches',sub:'Remove your saved search history',type:'action',key:'clearSearch'},
    {title:'Clear listening history',sub:'Remove recent tracks from this device',type:'action',key:'clearHistory'},
    {title:'Reset app preferences',sub:'Return to the default experience',type:'action',key:'resetSettings'},
    {title:'Privacy policy',sub:'Read how InstaMusic handles your data',type:'link',key:'privacy'},
    {title:'Open-source licenses',sub:'View third-party notices and licenses',type:'link',key:'licenses'},
    {title:'App version',sub:'Check for the latest available version',type:'action',key:'checkUpdates'},
    {title:'About InstaMusic',sub:'Product details and contact information',type:'link',key:'about'}
  ];

  let renderSetting=(item)=>{
    if(item.type==='switch'){
      let checked=item.key==='reminders'?remindersEnabled:(v[item.key]??item.default??false);
      return (0,g.jsx)(_,{icon:(0,g.jsx)(h,{className:`h-4 w-4`}),title:item.title,sub:item.sub,right:(0,g.jsx)(f,{
        'aria-label':item.title,checked:checked,
        onCheckedChange:val=>{
          if(item.key==='reminders')toggleReminders(val);
          else y(item.key,val);
        }
      })},item.key);
    }
    if(item.type==='select'){
      let options=item.key==='reminderFrequency'?['10 minutes','30 minutes','1 hour','Off']:item.key==='language'?['English','Malayalam','Hindi','Tamil','Telugu']:item.key==='streamQuality'?['Automatic','Standard','High']:item.key==='landingPage'?['Home','Search','Library']:[];
      let current=localStorage.getItem('im_setting_'+item.key)||options[0];
      return (0,g.jsxs)(_,{icon:(0,g.jsx)(h,{className:`h-4 w-4`}),title:item.title,sub:current+` • `+item.sub,right:(0,g.jsx)(`select`,{value:current,onChange:e=>{localStorage.setItem('im_setting_'+item.key,e.target.value);var b=window.MediaBridge||window.Android;if(item.key==='reminderFrequency'&&b&&typeof b.setReminderInterval==='function'){try{b.setReminderInterval(e.target.value)}catch(err){}}},className:`max-w-[120px] rounded-lg border border-input bg-surface p-2 text-xs`,children:options.map(o=>(0,g.jsx)(`option`,{value:o,children:o},o))})},item.key);
    }
    if(item.type==='action'){
      return (0,g.jsx)(l,{variant:`ghost`,className:`block h-auto w-full whitespace-normal p-0 text-left`,onClick:()=>{
        if(item.key==='clearSearch'){localStorage.removeItem('im_recent_searches');r.success('Recent searches cleared');}
        else if(item.key==='clearHistory'){localStorage.removeItem('im-listening-history-v1');r.success('Listening history cleared');}
        else if(item.key==='resetSettings'){localStorage.clear();r.success('Settings reset to defaults');window.location.reload();}
        else if(item.key==='checkUpdates'){var b=window.MediaBridge||window.Android;if(b&&typeof b.checkForUpdates==='function'){b.checkForUpdates();r.success('Checking for updates...');}else{r.success('InstaMusic is up to date (v1.0.40)');}}
        else if(item.key==='appVersion'){r.success('InstaMusic • Version 1.0.0');}
      },children:(0,g.jsx)(_,{icon:(0,g.jsx)(p,{className:`h-5 w-5`}),title:item.title,sub:item.sub,right:(0,g.jsx)(i,{className:`h-4 w-4 text-muted-foreground`})})},item.key);
    }
    if(item.type==='link'){
      return (0,g.jsx)(`button`,{onClick:()=>{
        if(item.key==='privacy'){
          setPrivacyModalOpen(true);
        }else if(item.key==='licenses'){
          alert('InstaMusic uses third-party open-source components. License details are available in the app repository.');
        }else if(item.key==='about'){
          alert('InstaMusic — your music, your mood.
Support: support@instamusic.app');
        }
      },className:`block w-full text-left hover:bg-accent`,children:(0,g.jsx)(_,{icon:(0,g.jsx)(u,{className:`h-5 w-5`}),title:item.title,sub:item.sub,right:(0,g.jsx)(i,{className:`h-4 w-4 text-muted-foreground`})})},item.key);
    }
  };

  return (0,g.jsxs)(`div`,{className:`pb-6`,children:[
    (0,g.jsx)(o,{title:`Settings`} ),
    (0,g.jsxs)(`div`,{className:`mx-4 mt-4 rounded-3xl border border-brand-orange/30 bg-gradient-to-r from-brand-orange/10 to-transparent p-4`,children:[
      (0,g.jsx)(`p`,{className:`text-xs font-extrabold uppercase tracking-[0.18em] text-brand-orange`,children:`Advanced Audio`}),
      (0,g.jsx)(`p`,{className:`mt-1 text-xs text-muted-foreground`,children:`Personalize your sound with a lightweight, browser-based equalizer. Effects apply where your device supports Web Audio.`}),
      [[`Bass`,bass,setBass], [`Vocal`,vocal,setVocal], [`Treble`,treble,setTreble]].map(([label,val,setter])=>(0,g.jsxs)(`div`,{className:`mt-4`,children:[
        (0,g.jsxs)(`div`,{className:`mb-1 flex justify-between text-xs font-semibold`,children:[(0,g.jsx)(`span`,{children:label}),(0,g.jsx)(`span`,{className:`text-brand-orange`,children:val+` dB`})]}),
        (0,g.jsx)(`input`,{type:`range`,min:-12,max:12,step:1,value:val,onChange:e=>{let n=parseFloat(e.target.value);setter(n);localStorage.setItem('im_eq_'+label.toLowerCase(),n);if(window.__im_setEqualizer)window.__im_setEqualizer(label==='Bass'?n:bass,label==='Vocal'?n:vocal,label==='Treble'?n:treble)},className:`w-full accent-brand-orange`})
      ]},label))
    ]}),
    (0,g.jsx)(`p`,{className:`px-4 pt-5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Feel & Access`} ),
    (0,g.jsxs)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface`,children:allSettings.slice(0,4).map(renderSetting)}),
    (0,g.jsx)(`p`,{className:`px-4 pt-5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Playback & Recommendations`} ),
    (0,g.jsxs)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface`,children:allSettings.slice(4,14).map(renderSetting)}),
    (0,g.jsx)(`p`,{className:`px-4 pt-5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Library & Display`} ),
    (0,g.jsxs)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface`,children:allSettings.slice(14,24).map(renderSetting)}),
    (0,g.jsx)(`p`,{className:`px-4 pt-5 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground`,children:`Notifications, Language & Data`} ),
    (0,g.jsxs)(`div`,{className:`mx-4 mt-2 divide-y divide-border overflow-hidden rounded-2xl border border-border bg-surface`,children:allSettings.slice(24).map(renderSetting)}),
    (0,g.jsx)(`div`,{className:`mt-8 flex flex-col items-center gap-1 text-center text-xs text-muted-foreground`,children:[(0,g.jsx)(s,{className:`h-4 w-4`}),(0,g.jsx)(`p`,{children:`INSTA MUSIC`})]}),
    (0,g.jsx)(`p`,{className:`mt-2 text-center text-[10px] text-muted-foreground`,children:`Settings are stored locally on this device.`})
  ]});
}

export{v as component};
