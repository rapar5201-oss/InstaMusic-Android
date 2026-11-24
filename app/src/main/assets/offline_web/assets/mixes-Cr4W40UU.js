var e=[
  "Malayalam Old Songs", "Malayalam 90s Golden Melodies", "Malayalam New Songs 2026",
  "Hindi Old Songs", "90s Bollywood Hits", "Hindi Romantic Songs", "Hindi New Chartbusters",
  "Tamil 80s Ilaiyaraaja Hits", "Tamil AR Rahman Classics", "Tamil New Songs 2026",
  "Telugu Old Melodies", "Telugu 90s Hits", "Telugu Top Hits 2026",
  "Old hit songs", "90s Golden hits", "80s Retro classics", "Evergreen melodies",
  "Ilaiyaraaja 80s", "Kishore Kumar golden hits", "Lata Mangeshkar classics", "SPB evergreen hits",
  "Mohammed Rafi classics", "Mukesh evergreen hits", "RD Burman retro", "Yesudas malayalam hits",
  "Chithra evergreen melodies", "Johnson Master classics", "Raveendran Master hits",
  "Late night drive", "Rainy day", "Gym beast mode", "Road trip", "Heartbreak", "Wedding dance",
  "Study focus", "Morning devotional", "Party anthems", "Sufi soulful peace", "Acoustic chill"
];

var t={
  malayalam: [
    "Malayalam Old Songs", "Malayalam 90s Hits", "Malayalam New Songs 2026", "Yesudas Evergreen", "Johnson Master Classics", "Chithra Melodies", "Raveendran Hits", "Vidyasagar Malayalam",
    "Manjummel Boys", "Aavesham", "Premalu", "Aadujeevitham", "Bramayugam", "Hridayam", "Lucifer", "Kumbalangi Nights", "Bangalore Days", "Premam", "Minnal Murali", "Drishyam", "Charlie", "Ustad Hotel", "Spadikam", "Devaragam", "Chithram", "Kireedam", "Manichitrathazhu", "Kilukkam", "His Highness Abdullah"
  ],
  tamil: [
    "Tamil 80s Hits", "Tamil 90s Hits", "Tamil New Songs 2026", "Ilaiyaraaja 80s Evergreens", "AR Rahman 90s Classics", "Anirudh Ravichander Hits", "SPB Tamil Hits", "Harris Jayaraj Melodies", "Yuvan Shankar Raja Hits",
    "Leo", "Jailer", "Vikram", "Devara Tamil", "GOAT", "Master", "Ponniyin Selvan", "Amaran", "Doctor", "Beast", "Varisu", "Thunivu", "Kaithi", "96", "Vikram Vedha", "Mersal", "Sarkar", "Theri", "Kaththi", "Thuppakki", "Mankatha", "Pudhupettai", "Nayagan", "Thalapathi", "Roja", "Bombay"
  ],
  telugu: [
    "Telugu Old Melodies", "Telugu 90s Hits", "Telugu Top Hits 2026", "SPB Telugu Hits", "Ilaiyaraaja Telugu", "Devi Sri Prasad Hits", "Thaman S Blockbusters", "Keeravani Classics",
    "Pushpa 2 The Rule", "Pushpa The Rise", "Devara Part 1", "Kalki 2898 AD", "RRR", "Salaar", "Bahubali 2", "Bahubali The Beginning", "Ala Vaikunthapurramuloo", "Sita Ramam", "Geetha Govindam", "Rangasthalam", "Arjun Reddy", "Sarileru Neekevvaru", "HanuMan", "Guntur Kaaram", "Waltair Veerayya", "Magadheera", "Pokiri"
  ],
  hindi: [
    "Hindi Old Songs", "90s Bollywood Hits", "Hindi Romantic Songs", "Hindi New Chartbusters", "Kishore Kumar Golden Hits", "Lata Mangeshkar Classics", "Mohammed Rafi Hits", "Mukesh Classics", "RD Burman Magic", "Arijit Singh Melodies", "KK Evergreen Hits", "Sonu Nigam Hits",
    "Pushpa 2 Hindi", "Devara Hindi", "Animal", "Jawan", "Pathaan", "Stree 2", "Brahmastra", "Kabir Singh", "Shershaah", "Dangal", "Aashiqui 2", "Rockstar", "Yeh Jawaani Hai Deewani", "3 Idiots", "Dilwale Dulhania Le Jayenge", "Kal Ho Naa Ho", "Om Shanti Om", "Jab We Met", "Zindagi Na Milegi Dobara", "Lagaan", "Swades", "Gadar 2", "Bajrangi Bhaijaan"
  ],
  kannada: [
    "Kannada Old Hits", "Dr Rajkumar Classics", "SPB Kannada Hits", "Kannada Top Hits 2026",
    "KGF Chapter 2", "KGF Chapter 1", "Kantara", "777 Charlie", "Kirik Party", "Ugramm", "Tagaru", "Mufti", "James", "Vikrant Rona"
  ],
  punjabi: [
    "Punjabi Hits 2026", "Sidhu Moosewala Evergreens", "Diljit Dosanjh Hits", "AP Dhillon", "Karan Aujla Top Tracks", "Gurdas Maan Classics",
    "Carry On Jatta 3", "Qismat", "Jatt and Juliet 3", "Warning 2"
  ],
  english: [
    "Global Top 50", "Billboard Hot 100", "Oldies But Goldies", "80s Synthpop Hits", "90s Rock Classics", "Acoustic Pop",
    "Michael Jackson", "Queen", "The Beatles", "Taylor Swift", "The Weeknd", "Coldplay", "Ed Sheeran", "Adele", "Eminem", "Billie Eilish", "Dua Lipa", "Imagine Dragons",
    "Spider-Man Across the Spider-Verse", "Barbie", "Interstellar", "Oppenheimer", "La La Land", "The Greatest Showman"
  ],
  bengali: [
    "Bengali Old Classics", "Hemanta Mukherjee Hits", "Manna Dey Bengali", "R D Burman Bengali", "Arijit Singh Bengali Hits",
    "Bela Shuru", "Projapoti", "Dawshom Awbotaar"
  ]
};

var n=e=>(e.length?e:["hindi","malayalam","tamil","telugu","english"]).flatMap(e=>t[e.toLowerCase()]??t["malayalam"]??[]);
var r="im-mix-history-v1";
var i=()=>{try{return JSON.parse(localStorage.getItem(r)??"[]")}catch{return[]}};
function a(e){try{localStorage.setItem(r,JSON.stringify([e,...i().filter(t=>t.q!==e.q)].slice(0,24)))}catch{}}
export{a as i,n,i as r,e as t};
