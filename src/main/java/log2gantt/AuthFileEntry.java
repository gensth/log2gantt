package log2gantt;
import java.util.Date;

/**
 * Class to store the parsed information of a auth.log file. comprises the
 * values of several lines (login-time, log-off-time,...).
 *
 * @author Michael Blume
 */
public class AuthFileEntry {
	private int processId;
	private String username;
	private Date loginTime;
	private Date logoffTime;
	private String daemon;
	private String method;

	public AuthFileEntry(int id, String name, Date login, Date logoff, String daemon, String method) {
		this.processId = id;
		this.username = name;
		this.loginTime = login;
		this.logoffTime = logoff;
		this.daemon = daemon;
		this.method = method;
	}

	public int getProcessId() {
		return processId;
	}

	public String getUsername() {
		return username;
	}

	public Date getLoginTime() {
		return this.loginTime;
	}

	public Date getLogoffTime() {
		return this.logoffTime;
	}

	public void setLogoffTime(Date logoffTime) {
	    this.logoffTime = logoffTime;
    }

	public String getDaemon() {
		return daemon;
	}

	public String getMethod() {
		return method;
	}
}