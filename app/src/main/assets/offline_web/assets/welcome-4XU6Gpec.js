import{n as e}from"./rolldown-runtime-CbXtAM7H.js";import{$ as t,E as n,It as r,Ot as i,Q as a,d as o,ln as s,rt as c,Tt as toast}from"./music-BLVe8YTA.js";import{i as l,n as u,r as d,t as f}from"./prefs-DwdEkQ3D.js";
var p=e(s()),m=r(),h=(e,t)=>e.includes(t)?e.filter(e=>e!==t):[...e,t];

function g(){
  let e=c(),r=i();
  let[mode,setMode]=(0,p.useState)(`auth`); // auth, prefs
  let[authTab,setAuthTab]=(0,p.useState)(`login`); // login, signup
  let[email,setEmail]=(0,p.useState)(``);
  let[password,setPassword]=(0,p.useState)(``);

  let[step,setStep]=(0,p.useState)(0);
  let[nameVal,setNameVal]=(0,p.useState)(e.profile.name===`Listener`?``:e.profile.name);
  let[langs,setLangs]=(0,p.useState)(e.prefs.languages);
  let[artists,setArtists]=(0,p.useState)(e.prefs.artists);
  let[moods,setMoods]=(0,p.useState)(e.prefs.moods);

  let finishPrefs=()=>{
    let t=nameVal.trim()||`Listener`;
    e.set(`profile`,e=>({...e,name:t,username:t.toLowerCase().replace(/[^a-z0-9]+/g,`.`)}));
    e.set(`prefs`,{languages:langs,artists:artists,moods:moods,onboarded:!0});
    r({to:`/home`});
  };

  let handleGuestLogin=()=>{
    e.set(`profile`,p=>({...p,name:`Guest Listener`,username:`guest.listener`,authProvider:`guest`}));
    e.set(`prefs`,p=>({...p,onboarded:!0}));
    toast.success(`Welcome! Instant guest mode activated.`);
    r({to:`/home`});
  };

  let handleGoogleLogin=()=>{
    e.set(`profile`,p=>({...p,name:`Google Listener`,username:`google.listener`,email:`listener@gmail.com`,authProvider:`google`}));
    e.set(`prefs`,p=>({...p,onboarded:!0}));
    toast.success(`Signed in with Google! Welcome back.`);
    r({to:`/home`});
  };

  let handleEmailSubmit=(ev)=>{
    ev.preventDefault();
    if(!email.trim()){
      toast.error(`Please enter your email address`);
      return;
    }
    if(!password||password.length<4){
      toast.error(`Please enter a password (min 4 characters)`);
      return;
    }
    let usernamePart=email.split(`@`)[0]||`Listener`;
    let capName=usernamePart.charAt(0).toUpperCase()+usernamePart.slice(1);
    e.set(`profile`,p=>({...p,name:capName,username:usernamePart.toLowerCase().replace(/[^a-z0-9]+/g,`.`),email:email.trim(),authProvider:`email`}));
    e.set(`prefs`,p=>({...p,onboarded:!0}));
    toast.success(authTab===`signup`?`Account created! Welcome, ${capName}.`:`Signed in! Welcome back, ${capName}.`);
    r({to:`/home`});
  };

  let T=[
    {title:`Hey there 👋`,sub:`What should we call you?`,ok:!0},
    {title:`Which languages do you love?`,sub:`Pick as many as you like.`,ok:langs.length>0},
    {title:`Who are your favourite artists?`,sub:`Choose at least one — we'll build your mix around them.`,ok:artists.length>0},
    {title:`What's your vibe?`,sub:`Pick moods you listen to most.`,ok:!0}
  ];
  let E=T[step];
  let nextStep=()=>step===T.length-1?finishPrefs():setStep(step+1);
  let artistList=l(langs.length?langs:Object.keys(f));

  if(mode===`auth`){
    return (0,m.jsxs)(`div`,{
      className:`relative mx-auto flex min-h-screen max-w-md flex-col justify-between px-6 pb-8 pt-7`,
      children:[
        (0,m.jsx)(`div`,{className:`pointer-events-none absolute inset-x-0 top-0 h-80 bg-brand-gradient opacity-25 blur-3xl`}),
        (0,m.jsxs)(`div`,{
          className:`relative flex flex-col items-center text-center`,
          children:[
            (0,m.jsx)(o,{}),
            (0,m.jsx)(`h1`,{className:`mt-4 text-3xl font-extrabold tracking-tight`,children:`Welcome to InstaMusic`}),
            (0,m.jsx)(`p`,{className:`mt-2 text-sm text-muted-foreground`,children:`Stream millions of songs, smart mixes & offline audio`})
          ]
        }),
        (0,m.jsxs)(`div`,{
          className:`relative my-6 space-y-4`,
          children:[
            // 1. Google Login
            (0,m.jsxs)(`button`,{
              type:`button`,
              onClick:handleGoogleLogin,
              className:`flex h-13 w-full items-center justify-center gap-3 rounded-2xl border border-border bg-surface px-4 text-sm font-bold shadow-sm transition hover:bg-surface-2 active:scale-[0.98]`,
              children:[
                (0,m.jsxs)(`svg`,{className:`h-5 w-5`,viewBox:`0 0 24 24`,children:[
                  (0,m.jsx)(`path`,{fill:`#4285F4`,d:`M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z`}),
                  (0,m.jsx)(`path`,{fill:`#34A853`,d:`M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z`}),
                  (0,m.jsx)(`path`,{fill:`#FBBC05`,d:`M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z`}),
                  (0,m.jsx)(`path`,{fill:`#EA4335`,d:`M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z`})
                ]}),
                `Continue with Google`
              ]
            }),

            // Divider
            (0,m.jsxs)(`div`,{
              className:`flex items-center gap-3 text-xs text-muted-foreground`,
              children:[
                (0,m.jsx)(`div`,{className:`h-px flex-1 bg-border`}),
                `or email & password`,
                (0,m.jsx)(`div`,{className:`h-px flex-1 bg-border`})
              ]
            }),

            // 2. Email + Password
            (0,m.jsxs)(`form`,{
              onSubmit:handleEmailSubmit,
              className:`space-y-2.5 rounded-2xl border border-border bg-surface/70 p-4 backdrop-blur`,
              children:[
                (0,m.jsxs)(`div`,{
                  className:`flex rounded-xl bg-surface-2 p-0.5 text-xs font-semibold`,
                  children:[
                    (0,m.jsx)(`button`,{
                      type:`button`,
                      onClick:()=>setAuthTab(`login`),
                      className:n(`flex-1 rounded-lg py-1.5 transition`,authTab===`login`?`bg-background text-foreground shadow-sm`:`text-muted-foreground`),
                      children:`Sign In`
                    }),
                    (0,m.jsx)(`button`,{
                      type:`button`,
                      onClick:()=>setAuthTab(`signup`),
                      className:n(`flex-1 rounded-lg py-1.5 transition`,authTab===`signup`?`bg-background text-foreground shadow-sm`:`text-muted-foreground`),
                      children:`Sign Up`
                    })
                  ]
                }),
                (0,m.jsx)(`input`,{
                  type:`email`,
                  required:!0,
                  placeholder:`Email address`,
                  value:email,
                  onChange:e=>setEmail(e.target.value),
                  className:`h-11 w-full rounded-xl border border-input bg-background px-3.5 text-sm outline-none focus:border-ring`
                }),
                (0,m.jsx)(`input`,{
                  type:`password`,
                  required:!0,
                  placeholder:`Password`,
                  value:password,
                  onChange:e=>setPassword(e.target.value),
                  className:`h-11 w-full rounded-xl border border-input bg-background px-3.5 text-sm outline-none focus:border-ring`
                }),
                (0,m.jsx)(`button`,{
                  type:`submit`,
                  className:`h-11 w-full rounded-xl bg-brand-gradient text-sm font-bold text-primary-foreground shadow-glow transition active:scale-[0.98]`,
                  children:authTab===`signup`?`Create Account`:`Sign In`
                })
              ]
            }),

            // 3. Guest Account (Instant, No Sign Up)
            (0,m.jsxs)(`button`,{
              type:`button`,
              onClick:handleGuestLogin,
              className:`group relative flex w-full flex-col items-center justify-center rounded-2xl border-2 border-brand-orange/40 bg-gradient-to-r from-brand-orange/15 to-brand-magenta/15 p-3.5 text-center transition hover:border-brand-orange active:scale-[0.98]`,
              children:[
                (0,m.jsxs)(`div`,{
                  className:`flex items-center gap-2 text-sm font-extrabold text-foreground`,
                  children:[
                    (0,m.jsx)(`span`,{children:`⚡`}),
                    `Continue as Guest`,
                    (0,m.jsx)(`span`,{className:`rounded-full bg-brand-gradient px-2 py-0.5 text-[10px] font-bold text-primary-foreground uppercase tracking-wider`,children:`No Sign Up`})
                  ]
                }),
                (0,m.jsx)(`p`,{className:`mt-1 text-xs text-muted-foreground`,children:`Instant access without entering email or password`})
              ]
            })
          ]
        }),

        // Bottom link to customize preferences
        (0,m.jsxs)(`div`,{
          className:`relative flex flex-col items-center gap-2 text-center text-xs text-muted-foreground`,
          children:[
            (0,m.jsx)(`button`,{
              type:`button`,
              onClick:()=>setMode(`prefs`),
              className:`font-semibold text-brand-blue hover:underline`,
              children:`Want to customize music languages & artists? Tap here →`
            })
          ]
        })
      ]
    });
  }

  // Preferences Mode (Languages, Artists, Moods)
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
          }):(0,m.jsx)(`button`,{
            "aria-label":`Back to login`,
            onClick:()=>setMode(`auth`),
            className:`grid h-10 w-10 place-items-center rounded-full hover:bg-accent`,
            children:(0,m.jsx)(a,{className:`h-5 w-5`})
          }),
          (0,m.jsxs)(`span`,{className:`text-xs text-muted-foreground`,children:[step+1,` / `,T.length]})
        ]
      }),
      (0,m.jsx)(`div`,{
        className:`relative mt-4 flex gap-1.5`,
        children:T.map((e,t)=>(0,m.jsx)(`div`,{className:n(`h-1 flex-1 rounded-full transition-all duration-500`,t<=step?`bg-brand-gradient`:`bg-muted`)},t))
      }),
      (0,m.jsxs)(`div`,{
        className:`relative mt-8 flex-1 animate-in fade-in slide-in-from-right-6 duration-300`,
        children:[
          (0,m.jsx)(`h1`,{className:`text-3xl font-extrabold leading-tight`,children:E.title}),
          (0,m.jsx)(`p`,{className:`mt-2 text-sm text-muted-foreground`,children:E.sub}),
          step===0&&(0,m.jsx)(`input`,{
            autoFocus:!0,
            value:nameVal,
            onChange:e=>setNameVal(e.target.value),
            onKeyDown:e=>e.key===`Enter`&&nextStep(),
            placeholder:`Your name`,
            "aria-label":`Your name`,
            className:`mt-8 h-14 w-full rounded-2xl border border-input bg-surface px-5 text-lg outline-none focus:border-ring`
          }),
          step===1&&(0,m.jsx)(`div`,{
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
          step===2&&(0,m.jsx)(`div`,{
            className:`mt-6 flex flex-wrap gap-2`,
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
          step===3&&(0,m.jsx)(`div`,{
            className:`mt-6 grid grid-cols-2 gap-3`,
            children:d.map(e=>{
              let t=moods.includes(e);
              return (0,m.jsx)(`button`,{
                onClick:()=>setMoods(h(moods,e)),
                className:n(`rounded-2xl border px-4 py-5 text-base font-bold transition active:scale-95`,t?`border-transparent bg-brand-gradient text-primary-foreground`:`border-border bg-surface hover:bg-surface-2`),
                children:e
              },e);
            })
          })
        ]
      },step),
      (0,m.jsx)(`button`,{
        disabled:!E.ok,
        onClick:nextStep,
        className:`relative mt-8 h-14 w-full rounded-full bg-brand-gradient text-base font-bold text-primary-foreground shadow-glow transition active:scale-[0.98] disabled:opacity-40`,
        children:step===T.length-1?`Start listening`:`Continue`
      })
    ]
  });
}

export{g as component};
