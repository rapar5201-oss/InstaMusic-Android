import{n as e}from"./rolldown-runtime-CbXtAM7H.js";import{Dt as t,G as n,It as r,Ot as i,U as a,_ as o,a as s,at as c,bt as l,ct as u,d,dt as f,g as p,i as m,it as h,ln as g,m as _,rt as v,st as y,t as b,X as HeartIcon,ot as OtQuery}from"./music-BLVe8YTA.js";import{t as x}from"./useQuery-DaqaePST.js";import{t as S}from"./sliders-horizontal-CrysWpKz.js";import{t as C}from"./sparkles-DpqmdoSQ.js";import{a as w}from"./prefs-DwdEkQ3D.js";import{n as T,r as E,t as D}from"./mixes-Cr4W40UU.js";var O=e(g()),k=r(),A=()=>{let e=new Date().getHours();return e<12?`Good morning`:e<17?`Good afternoon`:`Good evening`};function j({song:e,queue:t}){let r=v();return(0,k.jsxs)(`button`,{onClick:()=>r.play(e.id,t),className:`group w-36 shrink-0 snap-start text-left`,children:[(0,k.jsxs)(`div`,{className:`relative overflow-hidden rounded-2xl`,children:[(0,k.jsx)(`img`,{src:e.cover,alt:e.title,loading:`lazy`,className:`aspect-square w-full object-cover transition duration-500 group-hover:scale-105`}),(0,k.jsx)(`span`,{className:`absolute bottom-2 right-2 grid h-9 w-9 place-items-center rounded-full bg-brand-gradient text-primary-foreground opacity-90 shadow-glow transition group-active:scale-90`,children:(0,k.jsx)(n,{className:`ml-0.5 h-4 w-4 fill-current`})})]}),(0,k.jsx)(`p`,{className:`mt-2 line-clamp-1 text-sm font-semibold`,children:e.title})]})}function M({title:e,songs:t,loading:n}){if(!n&&t.length===0)return null;let r=t.map(e=>e.id);return(0,k.jsxs)(`section`,{className:`animate-in fade-in duration-500`,children:[(0,k.jsx)(_,{title:e}),(0,k.jsx)(m,{children:n?Array.from({length:4}).map((e,t)=>(0,k.jsxs)(`div`,{className:`w-36 shrink-0 snap-start`,children:[(0,k.jsx)(`div`,{className:`aspect-square w-36 skeleton-shimmer rounded-2xl`}),(0,k.jsx)(`div`,{className:`mt-2.5 h-3.5 w-28 skeleton-shimmer rounded-md`}),(0,k.jsx)(`div`,{className:`mt-1.5 h-2.5 w-20 skeleton-shimmer rounded-md`})]},t)):t.map(e=>(0,k.jsx)(j,{song:e,queue:r},e.id))})]})}var N=[`bg-brand-gradient`,`bg-cool-gradient`];function P({title:e,items:n}){return n.length?(0,k.jsxs)(`section`,{children:[(0,k.jsx)(_,{title:e}),(0,k.jsx)(m,{children:n.map((e,n)=>(0,k.jsxs)(t,{to:`/mix/$q`,params:{q:e.q},className:`relative grid aspect-square w-36 shrink-0 snap-start overflow-hidden rounded-2xl`,children:[e.cover?(0,k.jsx)(`img`,{src:e.cover,alt:``,loading:`lazy`,className:`absolute inset-0 h-full w-full object-cover`}):(0,k.jsx)(`span`,{className:`absolute inset-0 ${N[n%2]}`}),(0,k.jsx)(`span`,{className:`absolute inset-0 bg-gradient-to-t from-background/90 to-transparent`}),(0,k.jsx)(`span`,{className:`relative self-end p-3 text-sm font-extrabold capitalize leading-tight`,children:e.q.replace(/ movie$/,``)})]},e.q))})]}):null}function F({lang:e}){let t=x(c(w(e)));return(0,k.jsx)(M,{title:`Trending in ${w(e)}`,songs:t.data??[],loading:t.isLoading})}function I({mood:e,langs:t}){let n=x(y(e,t.map(w)));return(0,k.jsx)(M,{title:`${e} vibes`,songs:n.data??[],loading:n.isLoading})}function L(){let e=v(),r=i(),{languages:m,artists:g,moods:y,onboarded:j}=e.prefs,[N,L]=(0,O.useState)(`Hello`);
let[notifsOpen,setNotifsOpen]=(0,O.useState)(!1);
let[readNotifs,setReadNotifs]=(0,O.useState)(()=>{try{return JSON.parse(localStorage.getItem('im_read_notifs')||'[]')}catch{return[]}});(0,O.useEffect)(()=>L(A()),[]),(0,O.useEffect)(()=>{e.hydrated&&!j&&r({to:`/welcome`,replace:!0})},[e.hydrated,j,r]);let R=(0,O.useMemo)(()=>[`For You`,`Old Hits 📻`,...m.map(w),`Worldwide`],[m]),[z,B]=(0,O.useState)(`For You`),V=x(h(g)),H=x(f()),oldHitsQ=x(OtQuery(`old hit songs`)),ilaiyaraajaQ=x(OtQuery(`ilaiyaraaja 80s hits`)),kishoreQ=x(OtQuery(`kishore kumar golden hits`)),bollywood90sQ=x(OtQuery(`90s bollywood romantic`)),spbQ=x(OtQuery(`spb evergreen hits`)),legendsQ=x(OtQuery(`all time greatest hits`)),U=e.recent.map(l).filter(Boolean),W=e.userPosts.filter(t=>!e.hidden.includes(t.id)),G=x(u(m.map(w))),[K,q]=(0,O.useState)([]);(0,O.useEffect)(()=>q(E()),[]);let J=V.data?.[0]??H.data?.[0],Y=m.find(e=>w(e)===z),X=x({...c(Y?w(Y):``),enabled:!!Y});let notifsList=(0,O.useMemo)(()=>[
  {id:`n1`,icon:`🎵`,tag:`New Release`,title:`Your followed artist released a song`,body:`Anirudh Ravichander released a new track. Tap to stream.`,time:`10m ago`,song:J},
  {id:`n2`,icon:`💿`,tag:`Fresh Album`,title:`A new album or EP is available`,body:`Trending EP is available in high definition audio.`,time:`45m ago`,song:U[0]},
  {id:`n3`,icon:`✨`,tag:`Playlist Update`,title:`Your favourite playlist was updated`,body:`5 fresh viral tracks added to match your musical vibe.`,time:`2h ago`,song:H.data?.[1]},
  {id:`n4`,icon:`🔥`,tag:`Trending`,title:`A song in your playlist is trending`,body:`Trending #1 across listener charts this week.`,time:`4h ago`,song:V.data?.[1]},
  {id:`n5`,icon:`💜`,tag:`Smart Taste`,title:`A new song matches your music taste`,body:`Personalized recommendation matching your genres.`,time:`Yesterday`,song:G.data?.[0]}
],[J,U,H.data,V.data,G.data]);
let unreadNotifsCount=notifsList.filter(e=>!readNotifs.includes(e.id)).length;
let markAllNotifsRead=()=>{let ids=notifsList.map(e=>e.id);setReadNotifs(ids);try{localStorage.setItem('im_read_notifs',JSON.stringify(ids))}catch{}};
let playNotifTrack=(n)=>{if(n.song&&n.song.id)e.play(n.song.id,[n.song.id]);else if(J&&J.id)e.play(J.id,[J.id]);let u=[...readNotifs,n.id];setReadNotifs(u);try{localStorage.setItem('im_read_notifs',JSON.stringify(u))}catch{};setNotifsOpen(!1)};
return(0,k.jsxs)(`div`,{className:`pb-6`,children:[
notifsOpen&&(0,k.jsxs)(`div`,{
  className:`fixed inset-0 z-50 flex items-start justify-center bg-background/80 p-4 pt-14 backdrop-blur-md animate-in fade-in duration-200`,
  onClick:()=>setNotifsOpen(!1),
  children:[(0,k.jsxs)(`div`,{
    className:`relative flex max-h-[82vh] w-full max-w-md flex-col overflow-hidden rounded-3xl border border-border bg-surface shadow-2xl animate-in zoom-in-95 duration-200`,
    onClick:e=>e.stopPropagation(),
    children:[
      (0,k.jsxs)(`div`,{
        className:`flex items-center justify-between border-b border-border px-5 py-4`,
        children:[
          (0,k.jsxs)(`div`,{
            className:`flex items-center gap-2`,
            children:[
              (0,k.jsx)(HeartIcon,{className:`h-5 w-5 fill-brand-magenta text-brand-magenta`}),
              (0,k.jsx)(`h2`,{className:`text-lg font-bold`,children:`Notifications`}),
              unreadNotifsCount>0&&(0,k.jsx)(`span`,{className:`rounded-full bg-brand-magenta/20 px-2 py-0.5 text-xs font-semibold text-brand-magenta`,children:`${unreadNotifsCount} new`})
            ]
          }),
          (0,k.jsxs)(`div`,{
            className:`flex items-center gap-2`,
            children:[
              unreadNotifsCount>0&&(0,k.jsx)(`button`,{type:`button`,onClick:markAllNotifsRead,className:`text-xs font-semibold text-brand-blue hover:underline`,children:`Mark all read`}),
              (0,k.jsx)(`button`,{type:`button`,"aria-label":`Close`,onClick:()=>setNotifsOpen(!1),className:`grid h-8 w-8 place-items-center rounded-full bg-surface-2 text-muted-foreground hover:text-foreground`,children:`✕`})
            ]
          })
        ]
      }),
      (0,k.jsx)(`div`,{
        className:`divide-y divide-border/60 overflow-y-auto p-2`,
        children:notifsList.map(n=>{
          let isUnread=!readNotifs.includes(n.id);
          return (0,k.jsxs)(`div`,{
            onClick:()=>playNotifTrack(n),
            className:`group flex cursor-pointer items-start gap-3.5 rounded-2xl p-3.5 transition hover:bg-surface-2 active:scale-[0.99] ${isUnread?'bg-surface-2/60':''}`,
            children:[
              (0,k.jsx)(`span`,{className:`grid h-10 w-10 shrink-0 place-items-center rounded-xl bg-brand-gradient/15 text-lg`,children:n.icon}),
              (0,k.jsxs)(`div`,{
                className:`min-w-0 flex-1`,
                children:[
                  (0,k.jsxs)(`div`,{
                    className:`flex items-center justify-between gap-2`,
                    children:[
                      (0,k.jsx)(`span`,{className:`text-[10px] font-bold uppercase tracking-wider text-brand-orange`,children:n.tag}),
                      (0,k.jsx)(`span`,{className:`text-[11px] text-muted-foreground`,children:n.time})
                    ]
                  }),
                  (0,k.jsx)(`p`,{className:`text-sm font-bold leading-snug`,children:n.title}),
                  (0,k.jsx)(`p`,{className:`mt-0.5 text-xs text-muted-foreground`,children:n.body}),
                  (0,k.jsxs)(`div`,{
                    className:`mt-2.5 flex items-center gap-2`,
                    children:[
                      (0,k.jsx)(`span`,{className:`inline-flex items-center gap-1.5 rounded-full bg-brand-gradient px-3 py-1 text-[11px] font-bold text-primary-foreground shadow-glow`,children:`▶ Play Song`}),
                      isUnread&&(0,k.jsx)(`span`,{className:`h-2 w-2 rounded-full bg-brand-magenta`})
                    ]
                  })
                ]
              })
            ]
          },n.id);
        })
      })
    ]
  })]
}),(0,k.jsxs)(`header`,{className:`sticky top-0 z-30 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3 bg-background/80 px-4 py-3 backdrop-blur-xl lg:hidden`,children:[(0,k.jsx)(d,{}),(0,k.jsxs)(`div`,{className:`flex items-center gap-1`,children:[
  (0,k.jsxs)(`button`,{
    type:`button`,
    "aria-label":`Notifications`,
    onClick:()=>setNotifsOpen(!0),
    className:`relative grid h-10 w-10 place-items-center rounded-full text-foreground transition hover:bg-accent active:scale-95`,
    children:[
      (0,k.jsx)(HeartIcon,{className:`h-5 w-5 text-brand-magenta fill-brand-magenta/25 transition-transform hover:scale-110`}),
      unreadNotifsCount>0&&(0,k.jsx)(`span`,{className:`absolute top-2 right-2 h-2.5 w-2.5 rounded-full bg-brand-magenta ring-2 ring-background animate-pulse`})
    ]
  }),
  (0,k.jsx)(t,{to:`/search`,"aria-label":`Search`,className:`grid h-10 w-10 place-items-center rounded-full hover:bg-accent`,children:(0,k.jsx)(a,{className:`h-5 w-5`})}),(0,k.jsx)(t,{to:`/profile`,"aria-label":`Profile`,children:(0,k.jsx)(b,{name:e.profile.name,src:e.profile.avatar,tone:`bg-brand-gradient`,className:`h-9 w-9 ring-brand`})})]})]}),(0,k.jsxs)(`div`,{className:`flex items-end justify-between px-4 pt-3`,children:[(0,k.jsxs)(`div`,{children:[(0,k.jsxs)(`p`,{className:`text-sm text-muted-foreground`,children:[N,`,`]}),(0,k.jsx)(`h1`,{className:`text-2xl font-extrabold`,children:e.profile.name})]}),(0,k.jsxs)(t,{to:`/welcome`,className:`flex items-center gap-1.5 rounded-full border border-border px-3 py-1.5 text-xs font-semibold hover:bg-accent`,children:[(0,k.jsx)(S,{className:`h-3.5 w-3.5`}),`Tastes`]})]}),(0,k.jsx)(`div`,{className:`mt-4`,children:(0,k.jsx)(s,{items:R,value:z,onChange:B})}),z===`For You`&&(0,k.jsxs)(k.Fragment,{children:[J&&(0,k.jsxs)(`div`,{className:`relative mx-4 mt-5 overflow-hidden rounded-3xl border border-border animate-in fade-in zoom-in-95 duration-500`,children:[(0,k.jsx)(`img`,{src:J.cover,alt:``,className:`absolute inset-0 h-full w-full scale-125 object-cover opacity-50 blur-2xl`}),(0,k.jsxs)(`div`,{className:`relative grid grid-cols-[auto_minmax(0,1fr)] items-center gap-4 p-4`,children:[(0,k.jsx)(`img`,{src:J.cover,alt:J.title,className:`h-28 w-28 rounded-2xl object-cover shadow-glow`}),(0,k.jsxs)(`div`,{className:`min-w-0`,children:[(0,k.jsx)(`p`,{className:`text-[10px] font-semibold uppercase tracking-[0.2em] text-brand-orange`,children:`Picked for you`}),(0,k.jsx)(`p`,{className:`mt-1 line-clamp-2 text-lg font-extrabold leading-tight`,children:J.title}),(0,k.jsxs)(`button`,{onClick:()=>e.play(J.id,(V.data??H.data??[]).map(e=>e.id)),className:`mt-3 flex items-center gap-2 rounded-full bg-foreground px-4 py-2 text-sm font-bold text-background transition active:scale-95`,children:[(0,k.jsx)(n,{className:`h-4 w-4 fill-current`}),`Play mix`]})]})]})]}),(0,k.jsxs)(t,{to:`/discover`,className:`mx-4 mt-5 flex items-center gap-3 rounded-2xl border border-border bg-surface p-4 hover:bg-accent`,children:[(0,k.jsx)(`span`,{className:`grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-brand-gradient text-primary-foreground`,children:(0,k.jsx)(C,{className:`h-5 w-5`})}),(0,k.jsxs)(`span`,{className:`min-w-0`,children:[(0,k.jsx)(`span`,{className:`block font-bold`,children:`AI Mood Mix`}),(0,k.jsx)(`span`,{className:`block truncate text-xs text-muted-foreground`,children:`Describe your vibe — get a playlist instantly`})]})]}),K.length>0&&(0,k.jsx)(P,{title:`Your playlists lately`,items:K.map(e=>({q:e.q,cover:e.cover}))}),U.length>0&&(0,k.jsx)(M,{title:`Songs you've been hearing`,songs:U.slice(0,12)}),(0,k.jsx)(M,{title:`New releases`,songs:G.data??[],loading:G.isLoading}),(0,k.jsx)(M,{title:`📻 Golden Oldies & Evergreen Hits`,songs:oldHitsQ.data??[],loading:oldHitsQ.isLoading}),(0,k.jsx)(M,{title:`🎼 90s & 2000s Nostalgia Rewind`,songs:bollywood90sQ.data??[],loading:bollywood90sQ.isLoading}),(0,k.jsx)(M,{title:`👑 Maestro Ilaiyaraaja & SPB Classics`,songs:ilaiyaraajaQ.data??[],loading:ilaiyaraajaQ.isLoading}),(0,k.jsx)(M,{title:`🌟 Kishore Kumar & Lata Golden Era`,songs:kishoreQ.data??[],loading:kishoreQ.isLoading}),(0,k.jsx)(M,{title:`🎸 All-Time Legends & Global Classics`,songs:legendsQ.data??[],loading:legendsQ.isLoading}),(0,k.jsx)(P,{title:`Playlists for every vibe`,items:D.map(e=>({q:e}))}),(0,k.jsx)(P,{title:`Movie playlists`,items:T(m).map(e=>({q:`${e} movie`}))}),(0,k.jsx)(M,{title:`From your favourite artists`,songs:V.data??[],loading:V.isLoading}),m.map(e=>(0,k.jsx)(F,{lang:e},e)),y.slice(0,3).map(e=>(0,k.jsx)(I,{mood:e,langs:m},e)),!V.isLoading&&!H.isLoading&&!J&&(0,k.jsxs)(`div`,{className:`mx-4 mt-6 rounded-2xl border border-border bg-surface p-5 text-center`,children:[(0,k.jsx)(`p`,{className:`font-semibold`,children:`Songs are taking a moment to load`}),(0,k.jsx)(`p`,{className:`mt-1 text-sm text-muted-foreground`,children:`Pull down to refresh, or tap below.`}),(0,k.jsx)(`button`,{onClick:()=>{V.refetch(),H.refetch()},className:`mt-3 rounded-full bg-foreground px-4 py-2 text-sm font-bold text-background`,children:`Try again`})]}),(0,k.jsx)(_,{title:`Top charts worldwide`}),(0,k.jsx)(`div`,{children:(H.data??[]).slice(0,10).map((e,t,n)=>(0,k.jsx)(o,{song:e,index:t+1,queue:n.map(e=>e.id)},e.id))})]}),z===`Old Hits 📻`&&(0,k.jsxs)(k.Fragment,{children:[
  (0,k.jsxs)(`div`,{className:`relative mx-4 mt-4 overflow-hidden rounded-3xl border border-amber-500/30 bg-gradient-to-br from-amber-950/60 via-surface to-background p-5 shadow-2xl`,children:[
    (0,k.jsxs)(`div`,{className:`flex items-center gap-3`,children:[
      (0,k.jsx)(`span`,{className:`grid h-12 w-12 place-items-center rounded-2xl bg-amber-500/20 text-2xl`,children:`📻`}),
      (0,k.jsxs)(`div`,{className:`min-w-0 flex-1`,children:[
        (0,k.jsx)(`span`,{className:`text-[10px] font-bold uppercase tracking-widest text-amber-500`,children:`Retro Radio Station`}),
        (0,k.jsx)(`h2`,{className:`text-xl font-extrabold text-foreground`,children:`The Golden Era of Music`}),
        (0,k.jsx)(`p`,{className:`text-xs text-muted-foreground`,children:`Timeless 60s, 70s, 80s, 90s & 2000s all-time hits`})
      ]})
    ]}),
    (0,k.jsxs)(`div`,{className:`mt-4 flex flex-wrap gap-2`,children:[
      `90s Golden Hits`,`80s Retro Classics`,`Evergreen Melodies`,`Ilaiyaraaja 80s`,`Kishore Kumar Hits`,`SPB Classics`,`Lata & Rafi`,`All-Time Greatest Hits`
    ].map(item=>(0,k.jsx)(t,{to:`/mix/$q`,params:{q:item},className:`rounded-full border border-amber-500/30 bg-amber-500/10 px-3 py-1 text-xs font-semibold text-amber-200 transition hover:bg-amber-500/20 active:scale-95`,children:item},item))}),
    (oldHitsQ.data?.[0]||kishoreQ.data?.[0])&&(0,k.jsxs)(`button`,{
      onClick:()=>e.play((oldHitsQ.data?.[0]??kishoreQ.data?.[0]).id,(oldHitsQ.data??[]).concat(kishoreQ.data??[]).map(s=>s.id)),
      className:`mt-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-amber-500 to-orange-500 py-3 text-sm font-bold text-black shadow-lg transition active:scale-95`,
      children:[(0,k.jsx)(n,{className:`h-4 w-4 fill-current`}),`Play All Golden Hits`]
    })
  ]}),
  (0,k.jsx)(M,{title:`🌟 Golden Oldies & Evergreen Hits`,songs:oldHitsQ.data??[],loading:oldHitsQ.isLoading}),
  (0,k.jsx)(M,{title:`🎼 80s & 90s Maestro Ilaiyaraaja Magic`,songs:ilaiyaraajaQ.data??[],loading:ilaiyaraajaQ.isLoading}),
  (0,k.jsx)(M,{title:`🎙️ SPB Evergreen Classics`,songs:spbQ.data??[],loading:spbQ.isLoading}),
  (0,k.jsx)(M,{title:`💿 90s Bollywood Romantic Rewind`,songs:bollywood90sQ.data??[],loading:bollywood90sQ.isLoading}),
  (0,k.jsx)(M,{title:`🎤 Kishore Kumar & Lata Mangeshkar`,songs:kishoreQ.data??[],loading:kishoreQ.isLoading}),
  (0,k.jsx)(M,{title:`🌍 All-Time Global Classics & Rock Legends`,songs:legendsQ.data??[],loading:legendsQ.isLoading}),
  (0,k.jsx)(_,{title:`Top 30 All-Time Evergreen Chart`}),
  (0,k.jsx)(`div`,{children:(oldHitsQ.data??[]).concat(kishoreQ.data??[]).slice(0,30).map((e,t,n)=>(0,k.jsx)(o,{song:e,index:t+1,queue:n.map(e=>e.id)},e.id))})
]}),Y&&(0,k.jsxs)(k.Fragment,{children:[(0,k.jsx)(_,{title:`Top ${z} songs`}),X.isLoading&&(0,k.jsxs)(`div`,{className:`px-4 py-1`,children:[(0,k.jsxs)(`p`,{className:`text-xs font-medium text-muted-foreground mb-3`,children:[`Loading `,z,` songs…`]}),Array.from({length:5}).map((e,t)=>(0,k.jsxs)(`div`,{className:`grid grid-cols-[auto_auto_minmax(0,1fr)_auto] items-center gap-3 rounded-xl py-2`,children:[(0,k.jsx)(`span`,{className:`w-4 text-center text-xs font-mono text-muted-foreground/30`,children:t+1}),(0,k.jsx)(`div`,{className:`h-12 w-12 shrink-0 skeleton-shimmer rounded-lg`}),(0,k.jsxs)(`div`,{className:`min-w-0 flex flex-col gap-1.5`,children:[(0,k.jsx)(`div`,{className:`h-3.5 w-3/4 max-w-[180px] skeleton-shimmer rounded-md`}),(0,k.jsx)(`div`,{className:`h-2.5 w-1/2 max-w-[110px] skeleton-shimmer rounded-md`})]}),(0,k.jsx)(`div`,{className:`h-3 w-8 skeleton-shimmer rounded-md`})]},t))]}),X.data?.length===0&&(0,k.jsxs)(`p`,{className:`px-4 text-sm text-muted-foreground`,children:[`No `,z,` songs found right now.`]}),(0,k.jsx)(`div`,{children:(X.data??[]).map((e,t,n)=>(0,k.jsx)(o,{song:e,index:t+1,queue:n.map(e=>e.id)},e.id))})]}),z===`Worldwide`&&(0,k.jsxs)(k.Fragment,{children:[(0,k.jsx)(_,{title:`Top 20 worldwide right now`}),(0,k.jsx)(`div`,{children:(H.data??[]).map((e,t,n)=>(0,k.jsx)(o,{song:e,index:t+1,queue:n.map(e=>e.id)},e.id))})]}),W.length>0&&(0,k.jsxs)(k.Fragment,{children:[(0,k.jsx)(_,{title:`Your Music Moments`}),(0,k.jsx)(`div`,{className:`space-y-4`,children:W.map(e=>(0,k.jsx)(p,{m:e},e.id))})]})]})}export{L as component};