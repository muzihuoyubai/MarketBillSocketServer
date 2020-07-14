package club.banyuan.entity;

public class User {

  private int id;
  private String name;
  private int userType;
  private String pwd;
  private String pwdConfirm;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getUserType() {
    return userType;
  }

  public void setUserType(int userType) {
    this.userType = userType;
  }

  public String getUserTypeStr() {
    if (userType == 0) {
      return "普通用户";
    } else {
      return "经理";
    }
  }

  public String getPwd() {
    return pwd;
  }

  public void setPwd(String pwd) {
    this.pwd = pwd;
  }

  public String getPwdConfirm() {
    return pwdConfirm;
  }

  public void setPwdConfirm(String pwdConfirm) {
    this.pwdConfirm = pwdConfirm;
  }
}
