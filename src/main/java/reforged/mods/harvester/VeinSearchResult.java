package reforged.mods.harvester;

import reforged.mods.harvester.pos.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class VeinSearchResult {

    public static final VeinSearchResult NULL = abort(Type.ABORT_NULL);
    public static final VeinSearchResult OVER_LIMIT = abort(Type.ABORT_OVER_LIMIT);
    public static final VeinSearchResult NO_LEAVES = abort(Type.ABORT_NO_LEAVES);

    final List<BlockPos> positions;
    final Type type;

    public VeinSearchResult(List<BlockPos> result, Type type) {
        this.positions = result;
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public List<BlockPos> getPositions() {
        return this.positions;
    }

    public static VeinSearchResult success(List<BlockPos> result) {
        return new VeinSearchResult(result, Type.SUCCESS);
    }

    public static VeinSearchResult abort(Type type) {
        return new VeinSearchResult(new ArrayList<BlockPos>(), type);
    }

    public enum Type {
        SUCCESS,
        ABORT_OVER_LIMIT,
        ABORT_NO_LEAVES,
        ABORT_NULL
    }
}
