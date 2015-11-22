import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mount {
	public static char EMPTY = " ".charAt(0);

	public static List<String[]> getCommandOutput(String command)
			throws IOException, InterruptedException {
		String[] cmd = { "/bin/bash", "-c", command };
		List<String[]> result = new ArrayList<String[]>();
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		StringBuilder stringBuilder = new StringBuilder();
		String line = "";

		while ((line = reader.readLine()) != null) {
			int count = 0;
			String[] s = new String[20];
			String data = line.replaceAll("\\*", "");
			String[] xx = data.split(" ");
			for (int i = 0; i < xx.length; i++) {
				if (xx[i].length() > 0) {
					s[count] = xx[i];
					count++;
				}
			}
			stringBuilder.append(line + "\n");
			result.add(s);
		}
		return result;
	}

	private static String devName(String x) {
		return x.split("/")[2].substring(0, 3);
	}

	public static String getModelNumber(String dev) throws IOException,
			InterruptedException {
		String[] cmd = { "/bin/bash", "-c", "hdparm -I  /dev/" + dev };
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line = "";

		while ((line = reader.readLine()) != null) {
			if (line.indexOf("Model Number:") > 0) {
				String x = line.replaceAll("\t", "")
						.replace("Model Number:", "").trim();
				x = x.replaceAll(" ", "-");
				return x;
			}

		}
		return line;

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {

		Map<String, Integer> count = new HashMap<String, Integer>();
		Map<String, String> x = new HashMap<String, String>();
		List<String[]> data = getCommandOutput("fdisk -l  | grep dev | grep -v \"Disk\"  | grep -v \"sda\"");
		for (String[] line : data) {
			if (line[4].equals("5") || line[4].equals("f")) {
			} else {
				String dev = line[0];
				String devName = devName(dev);
				String name = x.get(devName);
				System.out.println(line[4]);
				if ("ee".equals(line[4])) {
					name = getModelNumber(devName);
					String command = "sgdisk /dev/" + devName
							+ " -p  | awk '$2 + 0 == $2'";
					List<String[]> partitions = getCommandOutput(command);
					for (String[] c : partitions) {
						System.out.println(c[0]);
						mountWithCount("/dev/" + devName + c[0], name,
								Integer.valueOf(c[0]));
					}
				} else {

					if (name == null) {
						name = getModelNumber(devName);
						x.put(devName, name);
					}

					Integer c = count.get(name);
					if (c == null) {
						c = 1;
					} else {
						c = c + 1;
					}
					count.put(name, c);
					mountWithCount(dev, name, c);
				}

			}

		}

	}

	private static void mountWithCount(String dev, String name, Integer c)
			throws IOException, InterruptedException {
		String targetDirect = "/mnt/" + name + "-" + c;
		File f = new File(targetDirect);
		if (!f.exists()) {
			f.mkdirs();
		}
		String command = "mount " + dev + "  " + targetDirect;
		executeCommand(command);
	}

	public static void executeCommand(String command) throws IOException,
			InterruptedException {
		System.out.println(command);
		String[] cmd = { "/bin/bash", "-c", command };
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		String z = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		while ((z = reader.readLine()) != null) {
			System.out.println(z);

		}
	}
}
