import{n as e}from"./rolldown-runtime-CbXtAM7H.js";import{$ as t,E as n,It as r,Ot as i,Q as a,d as o,ln as s,rt as c,Tt as toast}from"./music-BLVe8YTA.js";import{i as l,n as u,r as d,t as f}from"./prefs-DwdEkQ3D.js";
var p=e(s()),m=r(),h=(e,t)=>e.includes(t)?e.filter(e=>e!==t):[...e,t];

function g(){
  let e=c(),r=i();
  // Start directly in preferences/taste mode - NO social logins, NO login forms
  let[step,setStep]=(0,p.useState)(0);
  let[langs,setLangs]=(0,p.useState)(e.prefs.languages.length?e.prefs.languages:[`Malayalam`,`Tamil`,`Hindi`,`English`]);
  let[artists,setArtists]=(0,p.useState)(e.prefs.artists);
  let[moods,setMoods]=(0,p.useState)(e.prefs.moods.length?e.prefs.moods:[`Chill`,`Romantic`]);
  let[nameVal,setNameVal]=(0,p.useState)(e.profile.name===`Listener`?``:e.profile.name);

  let finishPrefs=()=>{
    let t=nameVal.trim()||`Music Lover`;
    e.set(`profile`,p=>({...p,name:t,username:t.toLowerCase().replace(/[^a-z0-9]+/g,`.`),authProvider:`direct`}));
    e.set(`prefs`,{languages:langs,artists:artists,moods:moods,onboarded:!0});
    toast.success(`Preferences saved! Welcome to InstaMusic, ${t}.`);
    r({to:`/home`});
  };

  let T=[
    {title:`Which languages do you love? 🎵`,sub:`Pick your music languages — we'll tailor your home mixes.`,ok:langs.length>0},
    {title:`Who are your favourite artists? 🌟`,sub:`Choose at least one artist to inspire your playlist.`,ok:artists.length>0},
    {title:`What's your vibe? ✨`,sub:`Pick moods you listen to most often.`,ok:moods.length>0},
    {title:`Hey there 👋`,sub:`What should we call you?`,ok:!0}
  ];
  let E=T[step];
  let nextStep=()=>step===T.length-1?finishPrefs():setStep(step+1);
  let artistList=l(langs.length?langs:Object.keys(f));

  return (0,m.jsxs)(`div`,{
    className:`relative mx-auto flex min-h-screen max-w-md flex-col px-6 pb-8 pt-5`,
    children:[
      (0,m.jsx)(`div`,{className:`pointer-events-none absolute inset-x-0 top-0 h-72 bg-brand-gradient opacity-20 blur-3xl`}),
      (0,m.jsxs)(`div`,{
        className:`relative flex items-center justify-between`,
        children:[
          step>0?(0,m.jsx)(`button`,{
            "aria-label":`Back`,
            onClick:()=>setStep(step-1),
            className:`grid h-10 w-10 place-items-center rounded-full hover:bg-accent`,
            children:(0,m.jsx)(a,{className:`h-5 w-5`})
          }):(0,m.jsx)(`div`,{className:`flex items-center gap-2`,children:[(0,m.jsx)(o,{className:`h-8 w-8`}),(0,m.jsx)(`span`,{className:`text-sm font-extrabold tracking-wider text-brand-orange`,children:`INSTAMUSIC`})]}),
          (0,m.jsxs)(`span`,{className:`text-xs font-semibold text-muted-foreground`,children:[step+1,` / `,T.length]})
        ]
      }),
      (0,m.jsx)(`div`,{
        className:`relative mt-4 flex gap-1.5`,
        children:T.map((e,t)=>(0,m.jsx)(`div`,{className:n(`h-1.5 flex-1 rounded-full transition-all duration-500`,t<=step?`bg-brand-gradient`:`bg-muted`)},t))
      }),
      (0,m.jsxs)(`div`,{
        className:`relative mt-8 flex-1 animate-in fade-in slide-in-from-right-6 duration-300`,
        children:[
          (0,m.jsx)(`h1`,{className:`text-2xl font-extrabold leading-tight`,children:E.title}),
          (0,m.jsx)(`p`,{className:`mt-2 text-sm text-muted-foreground`,children:E.sub}),
          step===0&&(0,m.jsx)(`div`,{
            className:`mt-6 grid grid-cols-2 gap-3`,
            children:u.map(e=>{
              let r=langs.includes(e.id);
              return (0,m.jsxs)(`button`,{
                onClick:()=>setLangs(h(langs,e.id)),
                className:n(`relative rounded-2xl border p-4 text-left transition active:scale-95`,r?`border-transparent bg-brand-gradient text-primary-foreground shadow-glow`:`border-border bg-surface hover:bg-surface-2`),
                children:[
                  (0,m.jsx)(`p`,{className:`text-lg font-bold`,children:e.native}),
                  (0,m.jsx)(`p`,{className:n(`text-xs`,r?`text-primary-foreground/85`:`text-muted-foreground`),children:e.name}),
                  r&&(0,m.jsx)(t,{className:`absolute right-3 top-3 h-4 w-4`})
                ]
              },e.id);
            })
          }),
          step===1&&(0,m.jsx)(`div`,{
            className:`mt-6 flex flex-wrap gap-2 max-h-[52vh] overflow-y-auto pr-1`,
            children:artistList.map(e=>{
              let r=artists.includes(e);
              return (0,m.jsxs)(`button`,{
                onClick:()=>setArtists(h(artists,e)),
                className:n(`flex items-center gap-2 rounded-full border px-4 py-2.5 text-sm font-semibold transition active:scale-95`,r?`border-transparent bg-brand-gradient text-primary-foreground`:`border-border bg-surface hover:bg-surface-2`),
                children:[
                  (0,m.jsx)(`span`,{className:n(`grid h-7 w-7 place-items-center rounded-full text-xs font-bold`,r?`bg-background/25`:`bg-cool-gradient text-primary-foreground`),children:e[0]}),
                  e,
                  r&&(0,m.jsx)(t,{className:`h-4 w-4`})
                ]
              },e);
            })
          }),
          step===2&&(0,m.jsx)(`div`,{
            className:`mt-6 grid grid-cols-2 gap-3`,
            children:d.map(e=>{
              let t=moods.includes(e);
              return (0,m.jsx)(`button`,{
                onClick:()=>setMoods(h(moods,e)),
                className:n(`rounded-2xl border px-4 py-5 text-base font-bold transition active:scale-95`,t?`border-transparent bg-brand-gradient text-primary-foreground`:`border-border bg-surface hover:bg-surface-2`),
                children:e
              },e);
            })
          }),
          step===3&&(0,m.jsxs)(`div`,{
            className:`mt-6 space-y-4`,
            children:[
              (0,m.jsx)(`input`,{
                autoFocus:!0,
                value:nameVal,
                onChange:e=>setNameVal(e.target.value),
                onKeyDown:e=>e.key===`Enter`&&nextStep(),
                placeholder:`Enter your name or nickname`,
                "aria-label":`Your name`,
                className:`h-14 w-full rounded-2xl border border-input bg-surface px-5 text-lg outline-none focus:border-ring`
              }),
              (0,m.jsx)(`p`,{className:`text-xs text-muted-foreground`,children:`No signup or password needed. You're ready to stream unlimited songs.`})
            ]
          })
        ]
      },step),
      (0,m.jsx)(`button`,{
        disabled:!E.ok,
        onClick:nextStep,
        className:`relative mt-8 h-14 w-full rounded-full bg-brand-gradient text-base font-bold text-primary-foreground shadow-glow transition active:scale-[0.98] disabled:opacity-40`,
        children:step===T.length-1?`Start listening 🎧`:`Continue`
      })
    ]
  });
}

export{g as component};
