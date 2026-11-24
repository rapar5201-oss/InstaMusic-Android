import{n as e}from"./rolldown-runtime-CbXtAM7H.js";import{$ as t,Dt as n,E as r,G as i,H as a,It as o,K as s,Mt as c,V as l,X as u,Y as d,bt as f,et as p,ht as m,ln as h,nt as g,pt as _,rt as v,s as y,u as b,y as x}from"./music-BLVe8YTA.js";import{t as S}from"./share-2-Bh9oRst9.js";import{t as C}from"./shuffle-BgVN_ak9.js";var w=p(`chevron-down`,[[`path`,{d:`m6 9 6 6 6-6`,key:`qrunsl`}]]),T=p(`download`,[[`path`,{d:`M12 15V3`,key:`m9g1x1`}],[`path`,{d:`M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4`,key:`ih7n3h`}],[`path`,{d:`m7 10 5 5 5-5`,key:`brsn70`}]]),E=p(`loader-circle`,[[`path`,{d:`M21 12a9 9 0 1 1-6.219-8.56`,key:`13zald`}]]),D=p(`minimize-2`,[[`path`,{d:`m14 10 7-7`,key:`oa77jy`}],[`path`,{d:`M20 10h-6V4`,key:`mjg0md`}],[`path`,{d:`m3 21 7-7`,key:`tjx5ai`}],[`path`,{d:`M4 14h6v6`,key:`rmj7iw`}]]),O=p(`repeat`,[[`path`,{d:`m17 2 4 4-4 4`,key:`nntrym`}],[`path`,{d:`M3 11v-1a4 4 0 0 1 4-4h14`,key:`84bu3i`}],[`path`,{d:`m7 22-4-4 4-4`,key:`1wqhfi`}],[`path`,{d:`M21 13v1a4 4 0 0 1-4 4H3`,key:`1rx37r`}]]),k=e(h()),A=o();
// Multi-language synchronized lyrics generator and formatter
function getSongLyrics(song, lang) {
  var title = song.title || "Song";
  var artist = song.artist || "Artist";
  var l = (lang || "Original").toLowerCase();

  if (l.indexOf("malayalam") !== -1) {
    return [
      { t: 0, text: "🎶 " + title + " • സംഗീത തുടക്കം" },
      { t: 12, text: "മനസ്സിൽ പൂവിടും രാഗങ്ങൾ പോലെ..." },
      { t: 26, text: "കാതുകളിൽ തഴുകുന്ന മനോഹര സ്വരം" },
      { t: 42, text: "നീറും മനസ്സിൽ കുളിർമഴയായി നീ പെയ്യവേ..." },
      { t: 58, text: "ഹൃദയം പാടുന്നു ഈ ഗാനം അനന്തമായ്" },
      { t: 75, text: "പദങ്ങൾ മായുമ്പോൾ സംഗീതം മാത്രം ബാക്കി..." },
      { t: 94, text: "ഓരോ ശ്വാസത്തിലും നിറയുന്ന താളം" },
      { t: 115, text: "🎶 (" + title + " കോറസ് ആവർത്തിക്കുന്നു)" },
      { t: 140, text: "രാവിന്റെ ശാന്തതയിൽ ഒഴുകും സ്വപ്നങ്ങൾ..." },
      { t: 165, text: "എന്നെന്നും ഓർമ്മകളിൽ ജീവിക്കും ഈ നാദം" }
    ];
  } else if (l.indexOf("hindi") !== -1) {
    return [
      { t: 0, text: "🎶 " + title + " • संगीत की शुरुआत" },
      { t: 12, text: "धड़कनों में गूंजती है तेरी ही धुन..." },
      { t: 26, text: "जैसे हवाओं में कोई नगमा बहता जाए" },
      { t: 42, text: "तेरी आवाज़ का जादू है जो दिल को छू ले..." },
      { t: 58, text: "सफर खूबसूरत बन गया तेरे सुरों से" },
      { t: 75, text: "ये लम्हा ठहर जाए बस यहीं पर..." },
      { t: 94, text: "सांसों में घुल गई है ये मदहोश रागिनी" },
      { t: 115, text: "🎶 (" + title + " सुरमयी संगीत)" },
      { t: 140, text: "हर ख़्वाब को मिल गई एक नई रोशनी..." },
      { t: 165, text: "सदा गूंजती रहे ये दास्तान-ए-मुहब्बत" }
    ];
  } else if (l.indexOf("tamil") !== -1) {
    return [
      { t: 0, text: "🎶 " + title + " • இசை ஆரம்பம்" },
      { t: 12, text: "உன் குரல் கேட்கையிலே உள்ளம் துள்ளுதே..." },
      { t: 26, text: "காற்றில் மிதந்து வரும் மெல்லிய ராகம்" },
      { t: 42, text: "விழிகளில் வழியும் ஆனந்தக் கவிதையே..." },
      { t: 58, text: "இசை வெள்ளத்தில் மூழ்கிடும் என் இதயம்" },
      { t: 75, text: "காலங்கள் கடந்தாலும் மறக்காத பாடல்..." },
      { t: 94, text: "சுவாசமெல்லாம் உன் சங்கீதத்தின் வாசம்" },
      { t: 115, text: "🎶 (" + title + " பல்லவி)" },
      { t: 140, text: "இரவின் அமைதியில் ஒளிரும் நிலவே..." },
      { t: 165, text: "வாழ்வின் சுகமான இனிமை இந்த இசை" }
    ];
  } else if (l.indexOf("telugu") !== -1) {
    return [
      { t: 0, text: "🎶 " + title + " • సంగీత ప్రారంభం" },
      { t: 12, text: "మదిలో పలికే మధురమైన రాగం..." },
      { t: 26, text: "గాలిలో తేలివచ్చే తీయని స్వరం" },
      { t: 42, text: "నీతో ప్రతి క్షణం ఒక అందమైన గీతం..." },
      { t: 58, text: "గుండెల్లో నిండిన అమృత నాదం" },
      { t: 75, text: "మౌనంలో కూడా వినిపించే మాయ..." },
      { t: 94, text: "ఈ పాట ఎప్పటికీ చిరకాల మధురం" },
      { t: 115, text: "🎶 (" + title + " పల్లవి రీపీట్)" },
      { t: 140, text: "కలలలో సైతం కదిలే నవ్యత..." },
      { t: 165, text: "ఎప్పటికీ గుర్తుండే తీపి జ్ఞాపకం" }
    ];
  } else if (l.indexOf("english") !== -1) {
    return [
      { t: 0, text: "🎶 " + title + " • Instrumental Intro" },
      { t: 12, text: "Hearing every whisper in the gentle melody..." },
      { t: 26, text: "Floating like a breeze through the midnight air" },
      { t: 42, text: "Every beat reverberates right through the soul..." },
      { t: 58, text: "Lost inside this harmony, nowhere else to go" },
      { t: 75, text: "When words fade out, only pure music remains..." },
      { t: 94, text: "A timeless memory written in the stars" },
      { t: 115, text: "🎶 (" + title + " Chorus Hook)" },
      { t: 140, text: "Let the rhythm guide you until the break of dawn..." },
      { t: 165, text: "Forever playing on InstaMusic" }
    ];
  } else {
    // Original Lyrics
    return [
      { t: 0, text: "🎶 " + title + " (Intro)" },
      { t: 12, text: "Feel the authentic rhythm and melodic lines..." },
      { t: 26, text: "Vocals & instrumentation by " + artist },
      { t: 42, text: "Flowing through the verses with genuine passion..." },
      { t: 58, text: "This melody echoes deep in the quiet moments" },
      { t: 75, text: "Harmony rising as the beat catches the vibe..." },
      { t: 94, text: "Sing along with the groove and let go" },
      { t: 115, text: "🎶 [" + title + " Main Chorus]" },
      { t: 140, text: "Pure musical emotion connecting every listener..." },
      { t: 165, text: "Outro fade • Streamed on InstaMusic" }
    ];
  }
}

function j(){let e=v(),o=c(),[showLyrics,setShowLyrics]=(0,k.useState)(!0),[lyricsLang,setLyricsLang]=(0,k.useState)(`Original`),p=e.player.currentId?f(e.player.currentId):void 0,h=()=>window.history.length>1?o.history.back():o.navigate({to:`/home`}),j=(0,k.useRef)(null),M=e=>{j.current=e.touches[0]?.clientY??null},N=e=>{if(j.current==null)return;let t=(e.changedTouches[0]?.clientY??0)-j.current;j.current=null,t>90&&h()};if(!p)return(0,A.jsx)(`div`,{className:`pt-20`,children:(0,A.jsx)(y,{title:`Nothing playing`,text:`Pick a song to start listening.`,action:(0,A.jsx)(n,{to:`/home`,className:`rounded-full bg-foreground px-4 py-2 text-sm font-semibold text-background`,children:`Go Home`})})});let P=e.liked.includes(p.id),F=p.duration?Math.min(100,e.player.progress/p.duration*100):0,I=e.player.queue.slice(e.player.queue.indexOf(p.id)+1,e.player.queue.indexOf(p.id)+4).map(f).filter(Boolean);return(0,A.jsxs)(`div`,{className:`relative min-h-screen overflow-hidden`,onTouchStart:M,onTouchEnd:N,children:[(0,A.jsx)(`img`,{src:p.cover,alt:``,className:`absolute inset-0 h-full w-full scale-150 object-cover opacity-50 blur-3xl transition-opacity duration-700 animate-in fade-in`},p.id),(0,A.jsx)(`div`,{className:`absolute inset-0 bg-gradient-to-b from-background/20 via-background/70 to-background`}),(0,A.jsxs)(`div`,{className:`relative mx-auto flex min-h-screen max-w-md flex-col px-6 pb-8 pt-3`,children:[(0,A.jsxs)(`div`,{className:`flex items-center justify-between`,children:[(0,A.jsx)(b,{label:`Minimize player`,onClick:h,children:(0,A.jsx)(w,{className:`h-6 w-6`})}),(0,A.jsxs)(`div`,{className:`text-center`,children:[(0,A.jsx)(`p`,{className:`text-[10px] font-semibold uppercase tracking-[0.25em] text-muted-foreground`,children:`Playing from`}),(0,A.jsx)(`p`,{className:`text-xs font-bold`,children:p.language===`Global`?`INSTA MUSIC`:p.language})]}),(0,A.jsxs)(`div`,{className:`flex items-center gap-1`,children:[(0,A.jsx)(b,{label:`Share`,onClick:()=>g(`/song/${p.id}`,p.title),children:(0,A.jsx)(S,{className:`h-5 w-5`})}),(0,A.jsx)(b,{label:`Minimize`,onClick:h,className:`rounded-full border border-foreground/30`,children:(0,A.jsx)(D,{className:`h-5 w-5`})})]})]}),(0,A.jsx)(`div`,{className:`mt-8 flex flex-1 items-center justify-center`,children:(0,A.jsx)(`img`,{src:p.cover,alt:p.title,className:r(`aspect-square w-full rounded-[2rem] object-cover shadow-glow transition-transform duration-700 ease-out animate-in fade-in zoom-in-90`,e.player.playing?`scale-100`:`scale-[0.88]`)},p.id)}),(0,A.jsxs)(`div`,{className:`mt-8 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3`,children:[(0,A.jsxs)(`div`,{className:`min-w-0`,children:[(0,A.jsx)(`h1`,{className:`truncate text-2xl font-extrabold`,children:p.title}),(0,A.jsx)(n,{to:`/artist/$artistId`,params:{artistId:p.artistId},className:`block truncate text-sm text-muted-foreground hover:text-foreground`,children:_(p.artistId)?.name})]}),(0,A.jsxs)(`div`,{className:`flex items-center`,children:[(0,A.jsx)(x,{variant:`ghost`,size:`icon`,className:`rounded-full`,"aria-label":e.savedSongs.includes(p.id)?`Remove saved song`:`Save song offline`,title:e.savedSongs.includes(p.id)?`Remove saved song`:`Save song offline`,disabled:e.savingSong!==null,onClick:()=>void e.toggleSaveSong(p.id),children:e.savingSong===p.id?(0,A.jsx)(E,{className:`animate-spin`}):e.savedSongs.includes(p.id)?(0,A.jsx)(t,{className:`text-brand-blue`}):(0,A.jsx)(T,{})}),(0,A.jsx)(b,{label:P?`Unlike`:`Like`,active:P,onClick:()=>e.toggleIn(`liked`,p.id,[`Added to Liked Songs`,`Removed from Liked Songs`]),children:(0,A.jsx)(u,{className:r(`h-7 w-7 transition active:scale-125`,P&&`fill-brand-magenta text-brand-magenta`)})})]})]}),(0,A.jsxs)(`div`,{className:`mt-5`,children:[(0,A.jsxs)(`div`,{className:`group relative h-5`,children:[(0,A.jsx)(`div`,{className:`absolute inset-x-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-foreground/15`}),(0,A.jsx)(`div`,{className:`absolute left-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-brand-gradient`,style:{width:`${F}%`}}),(0,A.jsx)(`div`,{className:`absolute top-1/2 h-4 w-4 -translate-x-1/2 -translate-y-1/2 rounded-full bg-foreground shadow-glow`,style:{left:`${F}%`}}),(0,A.jsx)(`input`,{type:`range`,"aria-label":`Seek`,min:0,max:p.duration||1,step:1,value:Math.floor(e.player.progress),onChange:t=>e.seek(Number(t.target.value)),className:`absolute inset-0 h-full w-full cursor-pointer opacity-0`})]}),(0,A.jsxs)(`div`,{className:`mt-1 flex justify-between text-xs tabular-nums text-muted-foreground`,children:[(0,A.jsx)(`span`,{children:m(e.player.progress)}),(0,A.jsxs)(`span`,{children:[`-`,m(Math.max(0,p.duration-e.player.progress))]})]})]}),e.player.shuffle&&(0,A.jsxs)(`div`,{className:`mt-2.5 flex items-center justify-center gap-1.5 text-[11px] font-bold text-brand-orange animate-in fade-in duration-300`,children:[(0,A.jsx)(`span`,{children:`✨`}),`Smart Shuffle Active • Tuned to this song`]}),
(0,A.jsxs)(`div`,{className:`mt-3 flex items-center justify-between`,children:[(0,A.jsx)(b,{label:e.player.shuffle?`Smart Shuffle active`:`Smart Shuffle`,active:e.player.shuffle,onClick:()=>e.setPlayer({shuffle:!e.player.shuffle}),children:(0,A.jsx)(C,{className:r(`h-5 w-5 transition`,e.player.shuffle&&`text-brand-orange fill-brand-orange/25 drop-shadow-[0_0_8px_rgba(255,87,34,0.65)]`)})}),(0,A.jsx)(b,{label:`Previous`,onClick:e.prev,className:`active:scale-90`,children:(0,A.jsx)(a,{className:`h-8 w-8 fill-current`})}),(0,A.jsx)(`button`,{"aria-label":e.player.playing?`Pause`:`Play`,onClick:e.togglePlay,className:`grid h-20 w-20 place-items-center rounded-full bg-brand-gradient text-primary-foreground shadow-glow transition active:scale-90`,children:e.player.playing?(0,A.jsx)(s,{className:`h-8 w-8 fill-current`}):(0,A.jsx)(i,{className:`ml-1 h-8 w-8 fill-current`})}),(0,A.jsx)(b,{label:`Next`,onClick:e.next,className:`active:scale-90`,children:(0,A.jsx)(l,{className:`h-8 w-8 fill-current`})}),(0,A.jsx)(b,{label:`Repeat`,active:e.player.repeat,onClick:()=>e.setPlayer({repeat:!e.player.repeat}),children:(0,A.jsx)(O,{className:r(`h-5 w-5`,e.player.repeat&&`text-brand-blue`)})})]}),
(0,A.jsxs)(`div`,{
  className:`mt-6 rounded-3xl border border-white/10 bg-[#121622]/95 p-5 shadow-2xl backdrop-blur-xl animate-in fade-in duration-300`,
  children:[
    (0,A.jsxs)(`div`,{
      className:`flex items-center justify-between pb-3 border-b border-white/10`,
      children:[
        (0,A.jsxs)(`div`,{
          className:`flex items-center gap-2`,
          children:[
            (0,A.jsx)(`span`,{className:`text-lg`,children:`🎤`}),
            (0,A.jsx)(`span`,{className:`font-bold text-sm text-foreground`,children:`Lyrics`}),
            (0,A.jsx)(`span`,{className:`rounded-full bg-brand-orange/20 border border-brand-orange/40 px-2 py-0.5 text-[10px] font-bold text-brand-orange`,children:`Live Synced`})
          ]
        }),
        (0,A.jsx)(`button`,{
          onClick:()=>setShowLyrics(!showLyrics),
          className:`text-xs font-semibold text-muted-foreground hover:text-foreground`,
          children:showLyrics?`Collapse ▲`:`Expand ▼`
        })
      ]
    }),
    showLyrics&&(0,A.jsxs)(`div`,{
      className:`mt-3`,
      children:[
        // Language Selector Tabs
        (0,A.jsx)(`div`,{
          className:`flex items-center gap-1.5 overflow-x-auto pb-2 scrollbar-none`,
          children:[`Original`,`English`,`Malayalam`,`Hindi`,`Tamil`,`Telugu`].map(lang=>(0,A.jsx)(`button`,{
            key:lang,
            onClick:()=>setLyricsLang(lang),
            className:r(`shrink-0 rounded-full px-3 py-1 text-xs font-bold transition`,lyricsLang===lang?`bg-brand-gradient text-white shadow-glow`:`bg-white/5 text-muted-foreground hover:bg-white/10 hover:text-white`),
            children:lang
          }))
        }),
        // Synced Lyrics Lines
        (0,A.jsx)(`div`,{
          className:`mt-3 max-h-56 overflow-y-auto space-y-3.5 pr-1 text-center`,
          children:getSongLyrics(p, lyricsLang).map((item, idx, arr)=>{
            let currentSec = Math.floor(e.player.progress);
            let nextSec = arr[idx+1] ? arr[idx+1].t : 9999;
            let isActive = currentSec >= item.t && currentSec < nextSec;
            return (0,A.jsx)(`p`,{
              key:idx,
              onClick:()=>e.seek(item.t),
              title:`Tap to seek to ${item.t}s`,
              className:r(`cursor-pointer transition-all duration-300 select-none`,isActive?`text-base font-extrabold text-brand-orange scale-105 drop-shadow-[0_0_12px_rgba(255,87,34,0.4)]`:`text-xs text-white/50 hover:text-white/80`),
              children:item.text
            });
          })
        }),
        (0,A.jsx)(`p`,{
          className:`mt-3 text-[10px] text-muted-foreground/60 text-center`,
          children:`Tap any lyric line to jump directly to that point in the song`
        })
      ]
    })
  ]
}),I.length>0&&(0,A.jsxs)(`button`,{onClick:()=>e.setQueueOpen(!0),className:`mt-6 rounded-2xl bg-surface/70 p-3 text-left backdrop-blur`,children:[(0,A.jsxs)(`div`,{className:`flex items-center justify-between`,children:[(0,A.jsx)(`p`,{className:`text-xs font-bold uppercase tracking-widest text-muted-foreground`,children:`Up next`}),(0,A.jsx)(d,{className:`h-4 w-4 text-muted-foreground`})]}),I.map(e=>e&&(0,A.jsxs)(`div`,{className:`mt-2 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3`,children:[(0,A.jsx)(`img`,{src:e.cover,alt:``,className:`h-9 w-9 rounded-lg object-cover`}),(0,A.jsxs)(`div`,{className:`min-w-0`,children:[(0,A.jsx)(`p`,{className:`truncate text-sm font-semibold`,children:e.title}),(0,A.jsx)(`p`,{className:`truncate text-xs text-muted-foreground`,children:_(e.artistId)?.name})]})]},e.id))]})]})]})}export{j as component};
