package com.example.myapplication;

import com.example.myapplication.slice.HelloPageAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

public class HelloPageAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(HelloPageAbilitySlice.class.getName());
    }
}
