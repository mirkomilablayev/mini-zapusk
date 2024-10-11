package com.example.util;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public enum Lang {

    ENGLISH("\uD83C\uDDFA\uD83C\uDDF8 English", "en"),
    RUSSIAN("\uD83C\uDDF7\uD83C\uDDFA Russian", "ru"),
    TURKISH("\uD83C\uDDF9\uD83C\uDDF7 Turkish", "tr"),
    UKRAINIAN("\uD83C\uDDFA\uD83C\uDDE6 Ukrainian", "uk"),
    UZBEK("\uD83C\uDDFA\uD83C\uDDFF Uzbekcha", "uz"),
    KAZAKH("\uD83C\uDDF0\uD83C\uDDFF Kazakh", "kk"),
    TURKMEN("\uD83C\uDDF9\uD83C\uDDF2 Turkmen", "tk"),
    TAJIK("\uD83C\uDDF9\uD83C\uDDEF Tojikcha", "tg"),
    KYRGYZ("\uD83C\uDDF0\uD83C\uDDEC Kyrgyz","ky"),
    SPANISH("\uD83C\uDDEA\uD83C\uDDF8 Spanish", "es"),
    ITALIAN("\uD83C\uDDEE\uD83C\uDDF9 Italian", "it"),
    FRENCH("\uD83C\uDDEB\uD83C\uDDF7 French","fr"),
    ARABIC("\uD83C\uDDE6\uD83C\uDDEA Arabic","ar"),
    POLISH("\uD83C\uDDF5\uD83C\uDDF1 Polish","pl")
    ;
    final String name;
    final String lan;
    Lang(String name, String lan){
        this.name = name;
        this.lan = lan;
    }

    public Lang findByName(String name){
         return Arrays.stream(Lang.values()).filter(lang -> Objects.equals(lang.name, name)).findFirst().orElse(UZBEK);
    }

    public Lang findByLan(String lan){
        return Arrays.stream(Lang.values()).filter(lang -> Objects.equals(lang.lan, lan)).findFirst().orElse(UZBEK);
    }

    public Boolean isExists(String name){
        boolean isExist = false;
        for (Lang lang : Arrays.stream(Lang.values()).collect(Collectors.toList())) {
            if (lang.getName().equalsIgnoreCase(name)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    public List<Lang> getAsList(String source, String target){
        return Arrays.stream(Lang.values()).filter(lang -> !Objects.equals(lang.name, source) && !Objects.equals(lang.name, target)).collect(Collectors.toList());
    }

}
