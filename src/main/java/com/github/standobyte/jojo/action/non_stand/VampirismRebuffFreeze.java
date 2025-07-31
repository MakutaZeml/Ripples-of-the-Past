package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.player.ContinuousActionInstance;
import com.github.standobyte.jojo.action.player.IPlayerAction;
import com.github.standobyte.jojo.action.stand.StandEntityAction;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.ZoomPunchEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.non_stand.hamon.ModHamonActions;
import com.github.standobyte.jojo.init.power.non_stand.vampirism.ModVampirismActions;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.util.mc.damage.DamageUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

import java.util.Optional;

public class VampirismRebuffFreeze extends VampirismAction implements IPlayerAction<VampirismRebuffFreeze.Instace, INonStandPower> {

    public VampirismRebuffFreeze(Builder builder){
        super(builder);
    }

    @Override
    protected ActionConditionResult checkSpecificConditions(LivingEntity user, INonStandPower power, ActionTarget target) {
        if (user.level.getDifficulty() == Difficulty.PEACEFUL) {
            return conditionMessage("peaceful");
        }
        if (user.isOnFire()) {
            return conditionMessage("fire");
        }
        if (user.level.dimensionType().ultraWarm()) {
            return conditionMessage("ultrawarm");
        }
        return ActionConditionResult.POSITIVE;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        Optional<Instace> currentFeeze = getCurFreeze(user);
        if(currentFeeze.isPresent()){
            Instace freeze = currentFeeze.get();
            if (!world.isClientSide()) {
                if (freeze.canCancel()) {
                    freeze.cancel();
                }
            }
        }else {
            if (!world.isClientSide()) {
                setPlayerAction(user, power);
            }
        }
    }

    @Override
    public boolean greenSelection(INonStandPower power, ActionConditionResult conditionCheck) {
        return getCurFreeze(power.getUser()).isPresent();
    }

    @Override
    public void setCooldownOnUse(INonStandPower power) {}

    @Override
    public Instace createContinuousActionInstance(LivingEntity user, PlayerUtilCap userCap, INonStandPower power) {
        return new Instace(user,userCap,power,this);
    }

    public static Optional<Instace> getCurFreeze(LivingEntity user) {
        return ContinuousActionInstance.getCurrentAction(user)
                .filter(action -> action.getAction() == ModVampirismActions.VAMPIRISM_FREEZE.get())
                .map(action -> (Instace) action);
    }

    @Override
    protected boolean canBeUsedDuringPlayerAction(ContinuousActionInstance<?, ?> curPlayerAction) {
        return curPlayerAction.getAction() == this && ((Instace) curPlayerAction).canCancel();
    }


    @Override
    protected void consumeEnergy(World world, LivingEntity user, INonStandPower power, ActionTarget target) {}

    public static class Instace extends ContinuousActionInstance<VampirismRebuffFreeze, INonStandPower>{
        private LivingEntity counterTarget;
        private DamageSource reduceDamage;
        private boolean canAttack = true;
        private boolean didAttack = false;
        public Instace(LivingEntity user, PlayerUtilCap userCap, INonStandPower playerPower, VampirismRebuffFreeze action) {
            super(user, userCap, playerPower, action);
        }


        @Override
        public void onStart() {
            super.onStart();
            setPhase(StandEntityAction.Phase.WINDUP);
        }

        private static final int WINDUP_TICKS = 12;
        public static final int COUNTER_TIMING_WINDOW = 10;
        private static final int PERFORM_TICKS = 12;
        private static final int RECOVERY_TICKS = 10;

        @Override
        protected void playerTick() {
            super.playerTick();
            switch (getPhase()){
                case WINDUP:
                    if(getTick() >= WINDUP_TICKS){
                        setPhase(StandEntityAction.Phase.PERFORM);
                    }
                    break;
                case PERFORM:
                    if(getTick()>= PERFORM_TICKS){
                        setPhase(StandEntityAction.Phase.RECOVERY);
                    }
                    break;
                case RECOVERY:
                    if (getTick() >= RECOVERY_TICKS) {
                        stopAction();
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }

        }


        @Override
        public boolean cancelIncomingDamage(DamageSource dmgSource, float dmgAmount) {
            LivingEntity meleeAttacker = DamageUtil.getMeleeAttacker(dmgSource);
            if ((meleeAttacker != null || dmgSource.getDirectEntity() instanceof ZoomPunchEntity)
                    && DamageUtil.isShieldBlockAngle(user, dmgSource)){
                boolean counterTiming = isCounterTiming();
                LivingEntity dealDamageTo = (LivingEntity) dmgSource.getEntity();
                if(dmgSource.getDirectEntity() instanceof ZoomPunchEntity){
                    dealDamageTo = ((ZoomPunchEntity)dmgSource.getDirectEntity()).getOwner();
                }
                VampirismRebuffFreeze action = getAction();
                float energyCost = action.getEnergyCost(playerPower,new ActionTarget(dealDamageTo));
                if(counterTiming && !(getScarlet(dealDamageTo).isPresent() && getScarlet(dealDamageTo).get().getPhase() == StandEntityAction.Phase.PERFORM)){
                    if(counterFreeze(dealDamageTo,dmgSource)){
                        setPhase(StandEntityAction.Phase.PERFORM, true);
                        return true;
                    }
                }
            }
            return super.cancelIncomingDamage(dmgSource, dmgAmount);
        }

        public boolean canCancel() {
            return getPhase() == StandEntityAction.Phase.WINDUP;
        }

        private boolean counterFreeze(LivingEntity target, DamageSource dmgSource){
            if (!didAttack && getPhase() == StandEntityAction.Phase.WINDUP ){
                this.counterTarget = target;
                freeze(target,true);
                user.playSound(ModSounds.VAMPIRE_FREEZE.get(), 1.0F, 1.0F);
                if(dmgSource.getMsgId().contains("hamon")){
                    return true;
                }
            }
            return false;
        }

        private void freeze(LivingEntity target, boolean properCounter){
            if(!canAttack){
                return;
            }
            if(!user.level.isClientSide){
                VampirismRebuffFreeze action = getAction();
                int difficulty = user.level.getDifficulty().getId();
                float damage = (float) Math.pow(2, difficulty) * 0.5f;
                if(DamageUtil.dealColdDamage(target, damage, user, null)){
                    EffectInstance freezeInstance = target.getEffect(ModStatusEffects.FREEZE.get());
                    if (freezeInstance == null) {
                        user.level.playSound(null, target, ModSounds.VAMPIRE_FREEZE.get(), target.getSoundSource(), 1.0F, 1.0F);
                        target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), (difficulty + 1) * 50, 0));
                    }
                    else {
                        int additionalDuration = (difficulty - 1) * 5 + 1;
                        int duration = freezeInstance.getDuration() + additionalDuration;
                        int lvl = duration / 100;
                        target.addEffect(new EffectInstance(ModStatusEffects.FREEZE.get(), duration, lvl));
                    }
                }
            }
        }

        public void cancel() {
            if (getPhase() == StandEntityAction.Phase.WINDUP) {
                float cdRatio = (float) getTick() / Instace.WINDUP_TICKS;
                cdRatio *= cdRatio;
                actionCooldown = (int) (actionCooldown * cdRatio);
                stopAction();
            }
        }


        public boolean isCounterTiming() {
            return getPhase() == StandEntityAction.Phase.WINDUP && getTick() >= WINDUP_TICKS - COUNTER_TIMING_WINDOW;
        }

        public static Optional<HamonSunlightYellowOverdrive.Instance> getScarlet(LivingEntity user) {
            return ContinuousActionInstance.getCurrentAction(user)
                    .filter(action -> action.getAction() == ModHamonActions.JONATHAN_SCARLET_OVERDRIVE.get())
                    .map(action -> (HamonSunlightYellowOverdrive.Instance) action);
        }
    }
}
