package org.nd4j.linalg.workspace;

import lombok.NonNull;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.memory.abstracts.Nd4jWorkspace;

import java.util.ArrayList;
import java.util.List;

/**
 * Workspace utility methods
 *
 * @author Alex Black
 */
public class WorkspaceUtils {

    private WorkspaceUtils() {
    }

    /**
     * Assert that no workspaces are currently open
     *
     * @param msg Message to include in the exception, if required
     */
    public static void assertNoWorkspacesOpen(String msg) throws ND4JWorkspaceException {
        if (Nd4j.getWorkspaceManager().anyWorkspaceActiveForCurrentThread()) {
            List<MemoryWorkspace> l = Nd4j.getWorkspaceManager().getAllWorkspacesForCurrentThread();
            List<String> workspaces = new ArrayList<>(l.size());
            for (MemoryWorkspace ws : l) {
                workspaces.add(ws.getId());
            }
            throw new ND4JWorkspaceException(msg + " - Open/active workspaces: " + workspaces);
        }
    }

    /**
     * Assert that the specified workspace is open and active
     *
     * @param ws       Name of the workspace to assert open and active
     * @param errorMsg Message to include in the exception, if required
     */
    public static void assertOpenAndActive(@NonNull String ws, @NonNull String errorMsg) throws ND4JWorkspaceException {
        if (!Nd4j.getWorkspaceManager().checkIfWorkspaceExistsAndActive(ws)) {
            throw new ND4JWorkspaceException(errorMsg);
        }
    }

    /**
     * Assert that the specified workspace is open, active, and is the current workspace
     *
     * @param ws       Name of the workspace to assert open/active/current
     * @param errorMsg Message to include in the exception, if required
     */
    public static void assertOpenActiveAndCurrent(@NonNull String ws, @NonNull String errorMsg) throws ND4JWorkspaceException {
        if (!Nd4j.getWorkspaceManager().checkIfWorkspaceExistsAndActive(ws)) {
            throw new ND4JWorkspaceException(errorMsg + " - workspace is not open and active");
        }
        MemoryWorkspace currWs = Nd4j.getMemoryManager().getCurrentWorkspace();
        if (currWs == null || !ws.equals(currWs.getId())) {
            throw new ND4JWorkspaceException(errorMsg + " - not the current workspace (current workspace: "
                    + (currWs == null ? null : currWs.getId()));
        }
    }

}
