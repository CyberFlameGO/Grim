package ac.grim.grimac.checks.impl.prediction;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.events.CompletePredictionEvent;
import ac.grim.grimac.utils.events.OffsetAlertEvent;
import ac.grim.grimac.utils.math.GrimMath;
import org.bukkit.Bukkit;

@CheckData(name = "Simulation", configName = "Simulation", decay = 0.02, dontAlertUntil = 100, alertInterval = 40)
public class OffsetHandler extends PostPredictionCheck {
    // Config
    double setbackDecayMultiplier;
    double threshold;
    double immediateSetbackThreshold;
    double maxAdvantage;
    double maxCeiling;

    // Current advantage gained
    double advantageGained = 0;

    public OffsetHandler(GrimPlayer player) {
        super(player);
    }

    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        double offset = predictionComplete.getOffset();

        CompletePredictionEvent completePredictionEvent = new CompletePredictionEvent(getPlayer(), predictionComplete.getOffset());
        Bukkit.getPluginManager().callEvent(completePredictionEvent);

        if (offset >= threshold || offset >= immediateSetbackThreshold) {
            advantageGained += offset;

            boolean isAlert = shouldAlert();
            boolean isSetback = advantageGained >= maxAdvantage || offset >= immediateSetbackThreshold;

            // Check check, String checkName, double offset, double violations, boolean vehicle, boolean isAlert, boolean isSetback
            OffsetAlertEvent event = new OffsetAlertEvent(this, getCheckName(), offset, getViolations(), player.inVehicle, isAlert, isSetback);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            giveOffsetLenienceNextTick(offset);

            if (isSetback) {
                player.getSetbackTeleportUtil().executeSetback();
            }

            violations++;
            alert("o: " + formatOffset(offset), getCheckName(), GrimMath.floor(violations) + "");

            advantageGained = Math.max(advantageGained, maxCeiling);
        } else {
            reward();
            advantageGained *= setbackDecayMultiplier;
        }

        removeOffsetLenience();
    }

    private void giveOffsetLenienceNextTick(double offset) {
        // Don't let players carry more than 1 offset into the next tick
        // (I was seeing cheats try to carry 1,000,000,000 offset into the next tick!)
        //
        // This value so that setting back with high ping doesn't allow players to gather high client velocity
        double minimizedOffset = Math.min(offset, 1);

        // Normalize offsets
        player.uncertaintyHandler.lastHorizontalOffset = minimizedOffset;
        player.uncertaintyHandler.lastVerticalOffset = minimizedOffset;
    }

    private void removeOffsetLenience() {
        player.uncertaintyHandler.lastHorizontalOffset = 0;
        player.uncertaintyHandler.lastVerticalOffset = 0;
    }

    @Override
    public void reload() {
        super.reload();
        setbackDecayMultiplier = getConfig().getDouble("Simulation.setback-decay-multiplier", 0.999);
        threshold = getConfig().getDouble("Simulation.threshold", 0.0001);
        immediateSetbackThreshold = getConfig().getDouble("Simulation.immediate-setback-threshold", 0.1);
        maxAdvantage = getConfig().getDouble("Simulation.max-advantage", 1);
        maxCeiling = getConfig().getDouble("Simulation.max-ceiling", 4);

        if (maxAdvantage == -1) setbackVL = Double.MAX_VALUE;
        if (immediateSetbackThreshold == -1) immediateSetbackThreshold = Double.MAX_VALUE;
    }

    public boolean doesOffsetFlag(double offset) {
        return offset >= threshold;
    }
}