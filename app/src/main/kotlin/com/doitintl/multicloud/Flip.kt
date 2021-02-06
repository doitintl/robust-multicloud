package com.doitintl.multicloud
    //https://stackoverflow.com/questions/24371977
     internal fun flip(s: String): String {
        var normal = "abcdefghijklmnopqrstuvwxyz_,;.?!/\\'"
        var split = "ɐqɔpǝɟbɥıظʞןɯuodbɹsʇnʌʍxʎz‾'؛˙¿¡/\\,"

        normal += "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        split += "∀qϽᗡƎℲƃHIſʞ˥WNOԀὉᴚS⊥∩ΛMXʎZ"

        normal += "0123456789"
        split += "0ƖᄅƐㄣϛ9ㄥ86"

        var newstr = ""
        var letter: Char
        for (i in 0 until s.length) {
            letter = s[i]
            val a = normal.indexOf(letter)
            newstr += if (a != -1) split[a] else letter
        }
        return StringBuilder(newstr).reverse().toString()
    }
