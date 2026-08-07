package com.example.util

import com.example.data.entity.VerseEntity

object InitialBibleVerses {
    val verses = listOf(
        // MORNING & INSPIRATION
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day which the LORD hath made; we will rejoice and be glad in it.",
            translation = "KJV", category = "Morning"
        ),
        VerseEntity(
            book = "Lamentations", chapter = 3, verseNumber = 22,
            text = "It is of the LORD's mercies that we are not consumed, because his compassions fail not. They are new every morning: great is thy faithfulness.",
            translation = "KJV", category = "Morning"
        ),
        VerseEntity(
            book = "Psalms", chapter = 5, verseNumber = 3,
            text = "My voice shalt thou hear in the morning, O LORD; in the morning will I direct my prayer unto thee, and will look up.",
            translation = "KJV", category = "Morning"
        ),
        VerseEntity(
            book = "Psalms", chapter = 143, verseNumber = 8,
            text = "Cause me to hear thy lovingkindness in the morning; for in thee do I trust: cause me to know the way wherein I should walk.",
            translation = "KJV", category = "Morning"
        ),
        VerseEntity(
            book = "Isaiah", chapter = 50, verseNumber = 4,
            text = "The Lord GOD hath given me the tongue of the learned... he wakeneth morning by morning, he wakeneth mine ear to hear.",
            translation = "KJV", category = "Morning"
        ),

        // STRENGTH & COURAGE
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I can do all things through Christ which strengtheneth me.",
            translation = "KJV", category = "Strength"
        ),
        VerseEntity(
            book = "Isaiah", chapter = 40, verseNumber = 31,
            text = "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary.",
            translation = "KJV", category = "Strength"
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.",
            translation = "KJV", category = "Courage"
        ),
        VerseEntity(
            book = "Psalms", chapter = 27, verseNumber = 1,
            text = "The LORD is my light and my salvation; whom shall I fear? the LORD is the strength of my life; of whom shall I be afraid?",
            translation = "KJV", category = "Strength"
        ),
        VerseEntity(
            book = "2 Timothy", chapter = 1, verseNumber = 7,
            text = "For God hath not given us the spirit of fear; but of power, and of love, and of a sound mind.",
            translation = "KJV", category = "Courage"
        ),

        // PEACE & REST
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 6,
            text = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God.",
            translation = "KJV", category = "Peace"
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 7,
            text = "And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus.",
            translation = "KJV", category = "Peace"
        ),
        VerseEntity(
            book = "John", chapter = 14, verseNumber = 27,
            text = "Peace I leave with you, my peace I give unto you: not as the world giveth, give I unto you. Let not your heart be troubled, neither let it be afraid.",
            translation = "KJV", category = "Peace"
        ),
        VerseEntity(
            book = "Matthew", chapter = 11, verseNumber = 28,
            text = "Come unto me, all ye that labour and are heavy laden, and I will give you rest.",
            translation = "KJV", category = "Peace"
        ),
        VerseEntity(
            book = "Psalms", chapter = 4, verseNumber = 8,
            text = "I will both lay me down in peace, and sleep: for thou, LORD, only makest me dwell in safety.",
            translation = "KJV", category = "Peace"
        ),

        // FAITH & HOPE
        VerseEntity(
            book = "Hebrews", chapter = 11, verseNumber = 1,
            text = "Now faith is the substance of things hoped for, the evidence of things not seen.",
            translation = "KJV", category = "Faith"
        ),
        VerseEntity(
            book = "Jeremiah", chapter = 29, verseNumber = 11,
            text = "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end.",
            translation = "KJV", category = "Hope"
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 5,
            text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
            translation = "KJV", category = "Faith"
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 6,
            text = "In all thy ways acknowledge him, and he shall direct thy paths.",
            translation = "KJV", category = "Faith"
        ),
        VerseEntity(
            book = "Romans", chapter = 15, verseNumber = 13,
            text = "Now the God of hope fill you with all joy and peace in believing, that ye may abound in hope, through the power of the Holy Ghost.",
            translation = "KJV", category = "Hope"
        ),

        // LOVE & GRACE
        VerseEntity(
            book = "1 Corinthians", chapter = 13, verseNumber = 4,
            text = "Charity suffereth long, and is kind; charity envieth not; charity vaunteth not itself, is not puffed up.",
            translation = "KJV", category = "Love"
        ),
        VerseEntity(
            book = "1 Corinthians", chapter = 13, verseNumber = 13,
            text = "And now abideth faith, hope, charity, these three; but the greatest of these is charity.",
            translation = "KJV", category = "Love"
        ),
        VerseEntity(
            book = "1 John", chapter = 4, verseNumber = 19,
            text = "We love him, because he first loved us.",
            translation = "KJV", category = "Love"
        ),
        VerseEntity(
            book = "Ephesians", chapter = 2, verseNumber = 8,
            text = "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God.",
            translation = "KJV", category = "Grace"
        ),
        VerseEntity(
            book = "2 Corinthians", chapter = 12, verseNumber = 9,
            text = "My grace is sufficient for thee: for my strength is made perfect in weakness.",
            translation = "KJV", category = "Grace"
        ),

        // JOY & PRAISE
        VerseEntity(
            book = "Psalms", chapter = 100, verseNumber = 1,
            text = "Make a joyful noise unto the LORD, all ye lands. Serve the LORD with gladness: come before his presence with singing.",
            translation = "KJV", category = "Joy"
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 1,
            text = "The LORD is my shepherd; I shall not want.",
            translation = "KJV", category = "Joy"
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 6,
            text = "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever.",
            translation = "KJV", category = "Joy"
        ),
        VerseEntity(
            book = "1 Thessalonians", chapter = 5, verseNumber = 16,
            text = "Rejoice evermore. Pray without ceasing. In every thing give thanks.",
            translation = "KJV", category = "Joy"
        ),
        VerseEntity(
            book = "Nehemiah", chapter = 8, verseNumber = 10,
            text = "Do not sorrow, for the joy of the LORD is your strength.",
            translation = "KJV", category = "Joy"
        ),

        // WEB TRANSLATION (WORLD ENGLISH BIBLE)
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day that Yahweh has made. We will rejoice and be glad in it.",
            translation = "WEB", category = "Morning"
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I can do all things through Christ, who strengthens me.",
            translation = "WEB", category = "Strength"
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Haven't I commanded you? Be strong and courageous. Don't be afraid, neither be dismayed: for Yahweh your God is with you wherever you go.",
            translation = "WEB", category = "Courage"
        ),
        VerseEntity(
            book = "Proverbs", chapter = 3, verseNumber = 5,
            text = "Trust in Yahweh with all your heart, and don't lean on your own understanding.",
            translation = "WEB", category = "Faith"
        ),
        VerseEntity(
            book = "John", chapter = 14, verseNumber = 27,
            text = "Peace I leave with you. My peace I give to you; not as the world gives, give I to you. Don't let your heart be troubled, neither let it be fearful.",
            translation = "WEB", category = "Peace"
        ),

        // BBE TRANSLATION (BIBLE IN BASIC ENGLISH)
        VerseEntity(
            book = "Psalms", chapter = 118, verseNumber = 24,
            text = "This is the day which the Lord has made; we will be glad and have joy in it.",
            translation = "BBE", category = "Morning"
        ),
        VerseEntity(
            book = "Philippians", chapter = 4, verseNumber = 13,
            text = "I have strength for all things through Christ who gives me power.",
            translation = "BBE", category = "Strength"
        ),
        VerseEntity(
            book = "Joshua", chapter = 1, verseNumber = 9,
            text = "Have I not given you your orders? Take heart and be strong; have no fear and do not be troubled; for the Lord your God is with you wherever you go.",
            translation = "BBE", category = "Courage"
        ),
        VerseEntity(
            book = "Psalms", chapter = 23, verseNumber = 1,
            text = "The Lord is my keeper; I will have no need.",
            translation = "BBE", category = "Joy"
        ),
        VerseEntity(
            book = "1 John", chapter = 4, verseNumber = 19,
            text = "We have love, because he first had love for us.",
            translation = "BBE", category = "Love"
        )
    )
}
