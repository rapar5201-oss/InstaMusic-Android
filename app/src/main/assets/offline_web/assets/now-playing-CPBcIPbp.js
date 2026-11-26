import{n as e}from"./rolldown-runtime-CbXtAM7H.js";import{$ as t,Dt as n,E as r,G as i,H as a,It as o,K as s,Mt as c,V as l,X as u,Y as d,bt as f,et as p,ht as m,ln as h,nt as g,pt as _,rt as v,s as y,u as b,y as x}from"./music-BLVe8YTA.js";import{t as S}from"./share-2-Bh9oRst9.js";import{t as C}from"./shuffle-BgVN_ak9.js";var w=p(`chevron-down`,[[`path`,{d:`m6 9 6 6 6-6`,key:`qrunsl`}]]),T=p(`download`,[[`path`,{d:`M12 15V3`,key:`m9g1x1`}],[`path`,{d:`M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4`,key:`ih7n3h`}],[`path`,{d:`m7 10 5 5 5-5`,key:`brsn70`}]]),E=p(`loader-circle`,[[`path`,{d:`M21 12a9 9 0 1 1-6.219-8.56`,key:`13zald`}]]),D=p(`minimize-2`,[[`path`,{d:`m14 10 7-7`,key:`oa77jy`}],[`path`,{d:`M20 10h-6V4`,key:`mjg0md`}],[`path`,{d:`m3 21 7-7`,key:`tjx5ai`}],[`path`,{d:`M4 14h6v6`,key:`rmj7iw`}]]),O=p(`repeat`,[[`path`,{d:`m17 2 4 4-4 4`,key:`nntrym`}],[`path`,{d:`M3 11v-1a4 4 0 0 1 4-4h14`,key:`84bu3i`}],[`path`,{d:`m7 22-4-4 4-4`,key:`1wqhfi`}],[`path`,{d:`M21 13v1a4 4 0 0 1-4 4H3`,key:`1rx37r`}]]),k=e(h()),A=o();
// Real multi-language synchronized lyrics engine with LrcLib API integration & Indic transliterator
function parseLrc(lrcText) {
  if (!lrcText || typeof lrcText !== 'string') return null;
  var lines = lrcText.split('\n');
  var out = [];
  var timeRegex = /\[(\d+):(\d+(?:\.\d+)?)\]/g;
  for (var i = 0; i < lines.length; i++) {
    var line = lines[i].trim();
    if (!line) continue;
    var matches = [];
    var m;
    timeRegex.lastIndex = 0;
    while ((m = timeRegex.exec(line)) !== null) {
      matches.push(parseInt(m[1], 10) * 60 + parseFloat(m[2]));
    }
    var clean = line.replace(/\[\d+:\d+(?:\.\d+)?\]/g, '').trim();
    if (clean && matches.length > 0) {
      for (var j = 0; j < matches.length; j++) {
        out.push({ t: Math.round(matches[j] * 10) / 10, text: clean });
      }
    }
  }
  out.sort(function(a, b) { return a.t - b.t; });
  return out.length > 0 ? out : null;
}

function romanizeIndic(text) {
  if (!text) return "";
  var out = "";
  for (var i = 0; i < text.length; i++) {
    var c = text.charCodeAt(i);
    if (c >= 0x0900 && c <= 0x097F) { // Devanagari
      var dMap = {
        0x0905:'a',0x0906:'aa',0x0907:'i',0x0908:'ee',0x0909:'u',0x090A:'oo',0x090B:'ri',
        0x090E:'e',0x090F:'e',0x0910:'ai',0x0912:'o',0x0913:'o',0x0914:'au',
        0x0915:'k',0x0916:'kh',0x0917:'g',0x0918:'gh',0x0919:'ng',
        0x091A:'ch',0x091B:'chh',0x091C:'j',0x091D:'jh',0x091E:'ny',
        0x091F:'t',0x0920:'th',0x0921:'d',0x0922:'dh',0x0923:'n',
        0x0924:'t',0x0925:'th',0x0926:'d',0x0927:'dh',0x0928:'n',
        0x092A:'p',0x092B:'ph',0x092C:'b',0x092D:'bh',0x092E:'m',
        0x092F:'y',0x0930:'r',0x0932:'l',0x0933:'l',0x0935:'v',0x0936:'sh',0x0937:'sh',0x0938:'s',0x0939:'ha',
        0x093E:'aa',0x093F:'i',0x0940:'ee',0x0941:'u',0x0942:'oo',0x0943:'ri',
        0x0947:'e',0x0948:'ai',0x094B:'o',0x094C:'au',0x094D:''
      };
      out += (dMap[c] !== undefined ? dMap[c] : text[i]);
    } else if (c >= 0x0D00 && c <= 0x0D7F) { // Malayalam
      var mMap = {
        0x0D05:'a',0x0D06:'aa',0x0D07:'i',0x0D08:'ee',0x0D09:'u',0x0D0A:'oo',
        0x0D0E:'e',0x0D0F:'e',0x0D10:'ai',0x0D12:'o',0x0D13:'o',0x0D14:'au',
        0x0D15:'k',0x0D16:'kh',0x0D17:'g',0x0D18:'gh',0x0D19:'ng',
        0x0D1A:'ch',0x0D1B:'chh',0x0D1C:'j',0x0D1D:'jh',0x0D1E:'ny',
        0x0D1F:'t',0x0D20:'th',0x0D21:'d',0x0D22:'dh',0x0D23:'n',
        0x0D24:'t',0x0D25:'th',0x0D26:'d',0x0D27:'dh',0x0D28:'n',
        0x0D2A:'p',0x0D2B:'ph',0x0D2C:'b',0x0D2D:'bh',0x0D2E:'m',
        0x0D2F:'y',0x0D30:'r',0x0D31:'r',0x0D32:'l',0x0D33:'l',0x0D34:'zh',0x0D35:'v',0x0D36:'sh',0x0D37:'sh',0x0D38:'s',0x0D39:'h',
        0x0D3E:'aa',0x0D3F:'i',0x0D40:'ee',0x0D41:'u',0x0D42:'oo',0x0D46:'e',0x0D47:'e',0x0D48:'ai',0x0D4A:'o',0x0D4B:'o',0x0D4C:'au',0x0D4D:''
      };
      out += (mMap[c] !== undefined ? mMap[c] : text[i]);
    } else if (c >= 0x0B80 && c <= 0x0BFF) { // Tamil
      var tMap = {
        0x0B85:'a',0x0B86:'aa',0x0B87:'i',0x0B88:'ee',0x0B89:'u',0x0B8A:'oo',
        0x0B8E:'e',0x0B8F:'e',0x0B90:'ai',0x0B92:'o',0x0B93:'o',0x0B94:'au',
        0x0B95:'k',0x0B99:'ng',0x0B9A:'ch',0x0B9E:'ny',0x0B9F:'t',0x0BA3:'n',
        0x0BA4:'th',0x0BA8:'n',0x0BAA:'p',0x0BAE:'m',0x0BAF:'y',0x0BB0:'r',0x0BB1:'r',0x0BB2:'l',0x0BB3:'l',0x0BB4:'zh',0x0BB5:'v',0x0BB7:'sh',0x0BB8:'s',0x0BB9:'h',
        0x0BBE:'aa',0x0BBF:'i',0x0BC0:'ee',0x0BC1:'u',0x0BC2:'oo',0x0BC6:'e',0x0BC7:'e',0x0BC8:'ai',0x0BCA:'o',0x0BCB:'o',0x0BCC:'au',0x0BCD:''
      };
      out += (tMap[c] !== undefined ? tMap[c] : text[i]);
    } else if (c >= 0x0C00 && c <= 0x0C7F) { // Telugu
      var teMap = {
        0x0C05:'a',0x0C06:'aa',0x0C07:'i',0x0C08:'ee',0x0C09:'u',0x0C0A:'oo',
        0x0C0E:'e',0x0C0F:'e',0x0C10:'ai',0x0C12:'o',0x0C13:'o',0x0C14:'au',
        0x0C15:'k',0x0C16:'kh',0x0C17:'g',0x0C18:'gh',0x0C19:'ng',
        0x0C1A:'ch',0x0C1B:'chh',0x0C1C:'j',0x0C1D:'jh',0x0C1E:'ny',
        0x0C1F:'t',0x0C20:'th',0x0C21:'d',0x0C22:'dh',0x0C23:'n',
        0x0C24:'t',0x0C25:'th',0x0C26:'d',0x0C27:'dh',0x0C28:'n',
        0x0C2A:'p',0x0C2B:'ph',0x0C2C:'b',0x0C2D:'bh',0x0C2E:'m',
        0x0C2F:'y',0x0C30:'r',0x0C31:'r',0x0C32:'l',0x0C33:'l',0x0C35:'v',0x0C36:'sh',0x0C37:'sh',0x0C38:'s',0x0C39:'h',
        0x0C3E:'aa',0x0C3F:'i',0x0C40:'ee',0x0C41:'u',0x0C42:'oo',0x0C46:'e',0x0C47:'e',0x0C48:'ai',0x0C4A:'o',0x0C4B:'o',0x0C4C:'au',0x0C4D:''
      };
      out += (teMap[c] !== undefined ? teMap[c] : text[i]);
    } else {
      out += text[i];
    }
  }
  return out;
}

function j(){let e=v(),o=c(),[showLyrics,setShowLyrics]=(0,k.useState)(!0),[lyricsLang,setLyricsLang]=(0,k.useState)(`Original`),p=e.player.currentId?f(e.player.currentId):void 0;
  let [lyricsState, setLyricsState]=(0,k.useState)({loading:false,synced:null,plain:null,songKey:'',source:'idle'});
  (0,k.useEffect)(()=>{
    if(!p||!p.title)return;
    let sKey=(p.id||'')+'::'+p.title+'::'+(p.artist||'');
    if(lyricsState.songKey===sKey)return;
    try{
      let cacheKey='im_real_lyrics_'+(p.id||p.title);
      let cached=localStorage.getItem(cacheKey);
      if(cached){
        let parsed=JSON.parse(cached);
        setLyricsState({loading:false,synced:parsed.synced||null,plain:parsed.plain||null,songKey:sKey,source:parsed.source||'cache'});
        return;
      }
    }catch(err){}
    setLyricsState(s=>({...s,loading:true,songKey:sKey}));
    var cleanT=p.title.replace(/\(.*?\)/g,'').replace(/\[.*?\]/g,'').replace(/feat\..*$/i,'').trim();
    var cleanA=(p.artist||'').replace(/\(.*?\)/g,'').replace(/,.*$/,'').trim();
    var getUrl='https://lrclib.net/api/get?track_name='+encodeURIComponent(cleanT)+(cleanA?'&artist_name='+encodeURIComponent(cleanA):'');
    fetch(getUrl,{headers:{'User-Agent':'InstaMusic/1.0'}}).then(r=>r.ok?r.json():null).then(d=>{
      if(d&&(d.syncedLyrics||d.plainLyrics))return d;
      var searchUrl='https://lrclib.net/api/search?q='+encodeURIComponent(cleanT+' '+cleanA);
      return fetch(searchUrl,{headers:{'User-Agent':'InstaMusic/1.0'}}).then(r=>r.ok?r.json():[]).then(list=>(Array.isArray(list)&&list.length>0)?list[0]:null);
    }).then(res=>{
      if(res&&(res.syncedLyrics||res.plainLyrics)){
        var synced=res.syncedLyrics?parseLrc(res.syncedLyrics):null;
        var plain=res.plainLyrics?res.plainLyrics.split('\n').filter(Boolean):null;
        var payload={synced:synced,plain:plain,source:'lrclib'};
        try{localStorage.setItem('im_real_lyrics_'+(p.id||p.title),JSON.stringify(payload));}catch(e){}
        setLyricsState({loading:false,synced:synced,plain:plain,songKey:sKey,source:'lrclib'});
      }else{
        setLyricsState({loading:false,synced:null,plain:null,songKey:sKey,source:'none'});
      }
    }).catch(()=>{
      setLyricsState({loading:false,synced:null,plain:null,songKey:sKey,source:'error'});
    });
  },[p?.id,p?.title,p?.artist]);let h=()=>window.history.length>1?o.history.back():o.navigate({to:`/home`}),j=(0,k.useRef)(null),M=e=>{j.current=e.touches[0]?.clientY??null},N=e=>{if(j.current==null)return;let t=(e.changedTouches[0]?.clientY??0)-j.current;j.current=null,t>90&&h()};if(!p)return(0,A.jsx)(`div`,{className:`pt-20`,children:(0,A.jsx)(y,{title:`Nothing playing`,text:`Pick a song to start listening.`,action:(0,A.jsx)(n,{to:`/home`,className:`rounded-full bg-foreground px-4 py-2 text-sm font-semibold text-background`,children:`Go Home`})})});let P=e.liked.includes(p.id),F=p.duration?Math.min(100,e.player.progress/p.duration*100):0,I=e.player.queue.slice(e.player.queue.indexOf(p.id)+1,e.player.queue.indexOf(p.id)+4).map(f).filter(Boolean);return(0,A.jsxs)(`div`,{className:`relative min-h-screen overflow-hidden`,onTouchStart:M,onTouchEnd:N,children:[(0,A.jsx)(`img`,{src:p.cover,alt:``,className:`absolute inset-0 h-full w-full scale-150 object-cover opacity-50 blur-3xl transition-opacity duration-700 animate-in fade-in`},p.id),(0,A.jsx)(`div`,{className:`absolute inset-0 bg-gradient-to-b from-background/20 via-background/70 to-background`}),(0,A.jsxs)(`div`,{className:`relative mx-auto flex min-h-screen max-w-md flex-col px-6 pb-8 pt-3`,children:[(0,A.jsxs)(`div`,{className:`flex items-center justify-between`,children:[(0,A.jsx)(b,{label:`Minimize player`,onClick:h,children:(0,A.jsx)(w,{className:`h-6 w-6`})}),(0,A.jsxs)(`div`,{className:`text-center`,children:[(0,A.jsx)(`p`,{className:`text-[10px] font-semibold uppercase tracking-[0.25em] text-muted-foreground`,children:`Playing from`}),(0,A.jsx)(`p`,{className:`text-xs font-bold`,children:p.language===`Global`?`INSTA MUSIC`:p.language})]}),(0,A.jsxs)(`div`,{className:`flex items-center gap-1`,children:[(0,A.jsx)(b,{label:`Share`,onClick:()=>g(`/song/${p.id}`,p.title),children:(0,A.jsx)(S,{className:`h-5 w-5`})}),(0,A.jsx)(b,{label:`Minimize`,onClick:h,className:`rounded-full border border-foreground/30`,children:(0,A.jsx)(D,{className:`h-5 w-5`})})]})]}),(0,A.jsx)(`div`,{className:`mt-8 flex flex-1 items-center justify-center`,children:(0,A.jsx)(`img`,{src:p.cover,alt:p.title,className:r(`aspect-square w-full rounded-[2rem] object-cover shadow-glow transition-transform duration-700 ease-out animate-in fade-in zoom-in-90`,e.player.playing?`scale-100`:`scale-[0.88]`)},p.id)}),(0,A.jsxs)(`div`,{className:`mt-8 grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3`,children:[(0,A.jsxs)(`div`,{className:`min-w-0`,children:[(0,A.jsx)(`h1`,{className:`truncate text-2xl font-extrabold`,children:p.title}),(0,A.jsx)(n,{to:`/artist/$artistId`,params:{artistId:p.artistId},className:`block truncate text-sm text-muted-foreground hover:text-foreground`,children:_(p.artistId)?.name})]}),(0,A.jsxs)(`div`,{className:`flex items-center`,children:[(0,A.jsx)(x,{variant:`ghost`,size:`icon`,className:`rounded-full`,"aria-label":e.savedSongs.includes(p.id)?`Remove saved song`:`Save song offline`,title:e.savedSongs.includes(p.id)?`Remove saved song`:`Save song offline`,disabled:e.savingSong!==null,onClick:()=>void e.toggleSaveSong(p.id),children:e.savingSong===p.id?(0,A.jsx)(E,{className:`animate-spin`}):e.savedSongs.includes(p.id)?(0,A.jsx)(t,{className:`text-brand-blue`}):(0,A.jsx)(T,{})}),(0,A.jsx)(b,{label:P?`Unlike`:`Like`,active:P,onClick:()=>e.toggleIn(`liked`,p.id,[`Added to Liked Songs`,`Removed from Liked Songs`]),children:(0,A.jsx)(u,{className:r(`h-7 w-7 transition active:scale-125`,P&&`fill-brand-magenta text-brand-magenta`)})})]})]}),(0,A.jsxs)(`div`,{className:`mt-5`,children:[(0,A.jsxs)(`div`,{className:`group relative h-5`,children:[(0,A.jsx)(`div`,{className:`absolute inset-x-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-foreground/15`}),(0,A.jsx)(`div`,{className:`absolute left-0 top-1/2 h-1.5 -translate-y-1/2 rounded-full bg-brand-gradient`,style:{width:`${F}%`}}),(0,A.jsx)(`div`,{className:`absolute top-1/2 h-4 w-4 -translate-x-1/2 -translate-y-1/2 rounded-full bg-foreground shadow-glow`,style:{left:`${F}%`}}),(0,A.jsx)(`input`,{type:`range`,"aria-label":`Seek`,min:0,max:p.duration||1,step:1,value:Math.floor(e.player.progress),onChange:t=>e.seek(Number(t.target.value)),className:`absolute inset-0 h-full w-full cursor-pointer opacity-0`})]}),(0,A.jsxs)(`div`,{className:`mt-1 flex justify-between text-xs tabular-nums text-muted-foreground`,children:[(0,A.jsx)(`span`,{children:m(e.player.progress)}),(0,A.jsxs)(`span`,{children:[`-`,m(Math.max(0,p.duration-e.player.progress))]})]})]}),e.player.shuffle&&(0,A.jsxs)(`div`,{className:`mt-2.5 flex items-center justify-center gap-1.5 text-[11px] font-bold text-brand-orange animate-in fade-in duration-300`,children:[(0,A.jsx)(`span`,{children:`✨`}),`Smart Shuffle Active • Tuned to this song`]}),
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
          className:`mt-3 max-h-64 overflow-y-auto space-y-3 pr-1 text-center scroll-smooth`,
          children:lyricsState.loading ? (0,A.jsxs)(`div`,{
            className:`py-8 text-center text-xs text-muted-foreground animate-pulse`,
            children:[`Finding synchronized lyrics for `, (0,A.jsx)(`span`,{className:`font-bold text-white`,children:p.title}), `...`]
          }) : lyricsState.synced && lyricsState.synced.length > 0 ? lyricsState.synced.map((item, idx, arr)=>{
            let currentSec = Math.floor(e.player.progress);
            let nextSec = arr[idx+1] ? arr[idx+1].t : 9999;
            let isActive = currentSec >= item.t && currentSec < nextSec;
            let lineText = (lyricsLang === 'English') ? romanizeIndic(item.text) : item.text;
            return (0,A.jsx)(`p`,{
              key:idx,
              onClick:()=>e.seek(item.t),
              title:`Tap to seek to ${Math.floor(item.t/60)}:${Math.floor(item.t%60).toString().padStart(2,'0')}`,
              className:r(`cursor-pointer transition-all duration-300 select-none py-1.5 px-3 rounded-xl`,isActive?`text-base font-black text-brand-orange scale-105 bg-brand-orange/15 shadow-glow`:`text-xs text-white/60 hover:text-white hover:bg-white/5`),
              children:lineText
            });
          }) : lyricsState.plain && lyricsState.plain.length > 0 ? (0,A.jsx)(`div`,{
            className:`space-y-2 py-2`,
            children:lyricsState.plain.map((line, idx)=>(0,A.jsx)(`p`,{
              key:idx,
              className:`text-xs text-white/85 leading-relaxed`,
              children:(lyricsLang === 'English') ? romanizeIndic(line) : line
            }))
          }) : (0,A.jsxs)(`div`,{
            className:`py-8 px-4 text-center rounded-2xl bg-white/5 border border-white/10`,
            children:[
              (0,A.jsx)(`p`,{className:`text-sm font-bold text-white`,children:`🎵 Lyrics unavailable online`}),
              (0,A.jsx)(`p`,{className:`mt-1 text-xs text-muted-foreground`,children:p.artist ? `Artist: ${p.artist}` : `Enjoy the music!`}),
              (0,A.jsx)(`p`,{className:`mt-2 text-[10px] text-white/40`,children:`Sync engine actively searches live archives for new releases`})
            ]
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
