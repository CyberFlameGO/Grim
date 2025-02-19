package ac.grim.grimac.predictionengine.predictions.rideable;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.predictionengine.predictions.PredictionEngineWaterLegacy;
import ac.grim.grimac.utils.data.VectorData;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;

public class PredictionEngineRideableWaterLegacy extends PredictionEngineWaterLegacy {
    Vector movementVector;

    public PredictionEngineRideableWaterLegacy(Vector movementVector) {
        this.movementVector = movementVector;
    }

    @Override
    public void addJumpsToPossibilities(GrimPlayer player, Set<VectorData> existingVelocities) {
        PredictionEngineRideableUtils.handleJumps(player, existingVelocities);
    }

    @Override
    public List<VectorData> applyInputsToVelocityPossibilities(GrimPlayer player, Set<VectorData> possibleVectors, float speed) {
        return PredictionEngineRideableUtils.applyInputsToVelocityPossibilities(movementVector, player, possibleVectors, speed);
    }

    @Override
    public Set<VectorData> fetchPossibleStartTickVectors(GrimPlayer player) {
        Set<VectorData> vectors = super.fetchPossibleStartTickVectors(player);

        return PredictionEngineRideableUtils.fetchPossibleStartTickVectors(player, vectors);
    }
}
