package com.example.util

import com.example.data.entity.VerseEntity

object InitialBibleVerses {
    val verses = listOf(
        // MORNING & INSPIRATION
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day which the LORD hath made; we will rejoice and be glad in it.",
            translation = "KJV", category = "Morning",
            prayer = "Ginoong Dios, salamat niining bag-ong adlaw nga Imong gihatag. Punoa ang among kasingkasing sa kalipay, grasya, ug paglaum karong adlawa. Amen."
        ),
        VerseEntity(
            book = "Lamentations", chapter = 3, verseNumber = 22,
            text = "It is of the LORD's mercies that we are not consumed, because his compassions fail not. They are new every morning: great is thy faithfulness.",
            translation = "KJV", category = "Morning",
            prayer = "O Dios nga Labing Gamhanan, salamat sa Imong walay katapusan nga kaluoy ug katinuman nga bag-o matag buntag sa among kinabuhi. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 5, verseNumber = 3,
            text = "My voice shalt thou hear in the morning, O LORD; in the morning will I direct my prayer unto thee, and will look up.",
            translation = "KJV", category = "Morning",
            prayer = "Ginoo, giyahi ang akong mga lakang ug dungga ang akong hangyo ug pag-ampo samtang nagasugod ako niining bag-ong buntag. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 143, verseNumber = 8,
            text = "Cause me to hear thy lovingkindness in the morning; for in thee do I trust: cause me to know the way wherein I should walk.",
            translation = "KJV", category = "Morning",
            prayer = "Amahan sa Langit, ipabati kanako ang Imong dako nga gugma ug ipakita kanako ang husto nga dalan nga akong agian karong adlawa. Amen."
        ),
        VerseEntity(
            book = "Isaiah", chapter = 50, verseNumber = 4,
            text = "The Lord GOD hath given me the tongue of the learned... he wakeneth morning by morning, he wakeneth mine ear to hear.",
            translation = "KJV", category = "Morning",
            prayer = "Ginoong Hesus, pukawa ang akong dunggan ug hunahuna aron makapaminaw ug makatuman ako sa Imong balaang pulong. Amen."
        ),

        // STRENGTH & COURAGE
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I can do all things through Christ which strengtheneth me.",
            translation = "KJV", category = "Strength",
            prayer = "Ginoong Hesus, salamat kay diha Kanimo anaa ang tanang kusog aron magmalampuson ako sa tanang pagsulay karong adlawa. Amen."
        ),
        VerseEntity(
            book = "Isaiah", chapter = 40, verseNumber = 31,
            text = "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary.",
            translation = "KJV", category = "Strength",
            prayer = "Dios nga Magbubuhat, bag-oha ang akong kusog ug katakus samtang nagasalig ug nagahulat ako sa Imong mga panalangin. Amen."
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.",
            translation = "KJV", category = "Courage",
            prayer = "Amahan sa Kaluwasan, wagtanga ang akong kahadlok kay nahibalo ako nga nag-uban Ka kanako sa bisan asa nga akong adtoan. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 27, verseNumber = 1,
            text = "The LORD is my light and my salvation; whom shall I fear? the LORD is the strength of my life; of whom shall I be afraid?",
            translation = "KJV", category = "Strength",
            prayer = "Ginoo, Ikaw ang akong kahayag ug panalipod. Diha sa Imong mga kamot ako nagatugyan sa akong tibuok kinabuhi. Amen."
        ),
        VerseEntity(
            book = "2 Timothy", chapter = 1, verseNumber = 7,
            text = "For God hath not given us the spirit of fear; but of power, and of love, and of a sound mind.",
            translation = "KJV", category = "Courage",
            prayer = "Salamat Dios sa Espiritu sa gahum, gugma, ug maayong panabot nga gihatag Mo alang sa among pamilya ug kaugalingon. Amen."
        ),

        // PEACE & REST
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 6,
            text = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
            translation = "KJV", category = "Peace",
            prayer = "Ginoo, gitugyan ko kanimo ang tanan kong kabalaka ug kasingkasing. Punoa ako sa Imong kalinaw samtang nagapasalamat ako Kanimo. Amen."
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 7,
            text = "And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.",
            translation = "KJV", category = "Peace",
            prayer = "Pahalunaa sa akong hunahuna ug kasingkasing ang kalinaw nga gikan sa Langit nga naglabaw sa tanang sabat sa tawo. Amen."
        ),
        VerseEntity(
            book = "John", chapter = 14, verseNumber = 27,
            text = "Peace I leave with you, my peace I give unto you: not as the world giveth, give I unto you. Let not your heart be troubled, neither let it be afraid.",
            translation = "KJV", category = "Peace",
            prayer = "Ginoong Hesus, dawaton ko ang Imong langitnong kalinaw aron dili maguol o mahadlok ang akong kasingkasing. Amen."
        ),
        VerseEntity(
            book = "Matthew", chapter = 11, verseNumber = 28,
            text = "Come unto me, all ye that labour and are heavy laden, and I will give you rest.",
            translation = "KJV", category = "Peace",
            prayer = "Hesus, moanhi ako Kanimo dala ang akong mga kabug-at ug kalisud. Hatagi ako sa Imong balaang kapahuwayan. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 4, verseNumber = 8,
            text = "I will both lay me down in peace, and sleep: for thou, LORD, only makest me dwell in safety.",
            translation = "KJV", category = "Peace",
            prayer = "Ginoo, salamat sa Imong pagpanalipod kanako sa adlaw ug sa gabii. Diha Kanimo magpahuway ako nga may kasiguruhan. Amen."
        ),

        // FAITH & HOPE
        VerseEntity(
            book = "Hebrews", chapter = 11, verseNumber = 1,
            text = "Now faith is the substance of things hoped for, the evidence of things not seen.",
            translation = "KJV", category = "Faith",
            prayer = "Dios sa Panalangin, dugangi ang akong pagtuo ug paglaum sa tanang mga vituwal ug mga saad nga dili pa nako karon makita. Amen."
        ),
        VerseEntity(
            book = "Jeremiah", chapter = 29, verseNumber = 11,
            text = "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end.",
            translation = "KJV", category = "Hope",
            prayer = "Amahan, salamat sa maayong kaugmaon ug mga plano sa kalinaw nga giandam Mo alang sa akong pamilya. Amen."
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 5,
            text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
            translation = "KJV", category = "Faith",
            prayer = "Ginoo, mosalig ako Kanimo sa tibuok kong kasingkasing ug ibutang ko ang akong kinabuhi diha sa Imong kamot. Amen."
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 6,
            text = "In all thy ways acknowledge him, and he shall direct thy paths.",
            translation = "KJV", category = "Faith",
            prayer = "Ginoong Dios, sa tanan kong mga plano ug lakang, ginakilala Ko Ikaw. Giyahi ug tultoli ang akong mga tunob. Amen."
        ),
        VerseEntity(
            book = "Romans", chapter = 15, verseNumber = 13,
            text = "Now the God of hope fill you with all joy and peace in believing, that ye may abound in hope, through the power of the Holy Ghost.",
            translation = "KJV", category = "Hope",
            prayer = "Dios sa Paglaum, punoa kami sa kalipay ug kalinaw pinaagi sa gahum sa Balaang Espiritu Santo. Amen."
        ),

        // LOVE & GRACE
        VerseEntity(
            book = "1 Corinthians", chapter = 13, verseNumber = 4,
            text = "Charity suffereth long, and is kind; charity envieth not; charity vaunteth not itself, is not puffed up.",
            translation = "KJV", category = "Love",
            prayer = "Ginoo, tudloi ako nga magmapailubon, magmabinantayon, ug maghigugmaay sa tanang mga tawo sa akong palibot. Amen."
        ),
        VerseEntity(
            book = "1 Corinthians", chapter = 13, verseNumber = 13,
            text = "And now abideth faith, hope, charity, these three; but the greatest of these is charity.",
            translation = "KJV", category = "Love",
            prayer = "Amahan, himoa ang Imong balaang gugma nga maghari kanunay sa akong kasingkasing ug panimalay. Amen."
        ),
        VerseEntity(
            book = "1 John", chapter = 4, verseNumber = 19,
            text = "We love him, because he first loved us.",
            translation = "KJV", category = "Love",
            prayer = "Salamat Ginoong Hesus sa Imong unang paghigugma kanamo. Tudloi kami nga maghigugmaay usab sa usag usa. Amen."
        ),
        VerseEntity(
            book = "Ephesians", chapter = 2, verseNumber = 8,
            text = "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God.",
            translation = "KJV", category = "Grace",
            prayer = "Amahan sa Langit, nagapasalamat ako sa Imong dili mahulagway nga grasya ug kaluwasan nga Imong gihatag kanamo. Amen."
        ),
        VerseEntity(
            book = "2 Corinthians", chapter = 12, verseNumber = 9,
            text = "My grace is sufficient for thee: for my strength is made perfect in weakness.",
            translation = "KJV", category = "Grace",
            prayer = "Ginoo, salamat kay ang Imong grasya igo ug perpekto diha sa tanan kong kabag-ohan ug kaatbang. Amen."
        ),

        // JOY & PRAISE
        VerseEntity(
            book = "Psalms", chapter = 100, verseNumber = 1,
            text = "Make a joyful noise unto the LORD, all ye lands. Serve the LORD with gladness: come before his presence with singing.",
            translation = "KJV", category = "Joy",
            prayer = "Dios nga Labing Halangdon, moalagad ug modayeg ako Kanimo uban ang dakong kalipay sa akong kalag! Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 1,
            text = "The LORD is my shepherd; I shall not want.",
            translation = "KJV", category = "Joy",
            prayer = "Ginoong Hesus, Ikaw ang akong Maayong Magbalantay. Nahibalo ako nga wala akoy kakuwangan diha sa Imong panalipod. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 6,
            text = "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever.",
            translation = "KJV", category = "Joy",
            prayer = "Ginoo, salamat sa Imong maayong kabubut-on ug kaluoy nga mag-uban kanako sa tanang adlaw sa akong kinabuhi. Amen."
        ),
        VerseEntity(
            book = "1 Thessalonians", chapter = 5, verseNumber = 16,
            text = "Rejoice evermore. Pray without ceasing. In every thing give thanks.",
            translation = "KJV", category = "Joy",
            prayer = "Ginoo, hatagi ako sa kasingkasing nga magpasalamat ug mag-ampo kanunay diha sa tanang higayon. Amen."
        ),
        VerseEntity(
            book = "Nehemiah", chapter = 8, verseNumber = 10,
            text = "Do not sorrow, for the joy of the LORD is your strength.",
            translation = "KJV", category = "Joy",
            prayer = "Amahan, ang kalipay gikan Kanimo mao ang akong dako nga kusog batok sa tanang kasubo. Amen."
        ),

        // WEB TRANSLATION (WORLD ENGLISH BIBLE)
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day that Yahweh has made. We will rejoice and be glad in it.",
            translation = "WEB", category = "Morning",
            prayer = "Lord God, thank You for this day You have made. Fill our hearts with joy and gladness as we follow You today. Amen."
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I can do all things through Christ, who strengthens me.",
            translation = "WEB", category = "Strength",
            prayer = "Lord Jesus, empower me with Your strength to face every obstacle and victory today. Amen."
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Haven't I commanded you? Be strong and courageous. Don't be afraid, neither be dismayed: for Yahweh your God is with you wherever you go.",
            translation = "WEB", category = "Courage",
            prayer = "Heavenly Father, grant me courage and peace knowing You walk with me wherever I go. Amen."
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 5,
            text = "Trust in Yahweh with all your heart, and don't lean on your own understanding.",
            translation = "WEB", category = "Faith",
            prayer = "Lord, I place my full trust in You and surrender my own understanding to Your divine wisdom. Amen."
        ),
        VerseEntity(
            book = "John", chapter = 14, verseNumber = 27,
            text = "Peace I leave with you. My peace I give to you; not as the world gives, give I to you. Don't let your heart be troubled, neither let it be fearful.",
            translation = "WEB", category = "Peace",
            prayer = "Jesus, I welcome Your divine peace into my heart, leaving behind all worry and fear. Amen."
        ),

        // BBE TRANSLATION (BIBLE IN BASIC ENGLISH)
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day which the Lord has made; we will be glad and have joy in it.",
            translation = "BBE", category = "Morning",
            prayer = "Thank You Lord for creating this new day. May our souls be filled with praise and thanksgiving. Amen."
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I have strength for all things through Christ who gives me power.",
            translation = "BBE", category = "Strength",
            prayer = "Lord, grant me strength for all tasks ahead through Christ who empowers me. Amen."
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Have I not given you your orders? Take heart and be strong; have no fear and do not be troubled; for the Lord your God is with you wherever you go.",
            translation = "BBE", category = "Courage",
            prayer = "Lord, remove all my trouble and fear, and strengthen my soul for the day. Amen."
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 1,
            text = "The Lord is my keeper; I will have no need.",
            translation = "BBE", category = "Joy",
            prayer = "The Lord is my guardian and keeper; in His care I am fully provided for. Amen."
        ),
        VerseEntity(
            book = "1 John", chapter = 4, verseNumber = 19,
            text = "We have love, because he first had love for us.",
            translation = "BBE", category = "Love",
            prayer = "Father, thank You for loving us first. Help us share that unconditional love with others. Amen."
        )
    )
}

