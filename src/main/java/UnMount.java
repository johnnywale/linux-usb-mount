import java.io.IOException;
import java.util.List;

public class UnMount {
	public static void main(String[] args) throws IOException,
			InterruptedException {
		List<String[]> data = Mount.getCommandOutput("mount | grep /mnt");
		for (String[] x : data) {
			Mount.executeCommand("umount " + x[2]);
		}
	}
}
