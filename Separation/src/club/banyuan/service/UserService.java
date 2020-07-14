package club.banyuan.service;

import club.banyuan.entity.User;
import club.banyuan.exception.RequestException;
import club.banyuan.http.HttpStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserService {

  private static List<User> userList = new ArrayList<>();
  private static int userId = 1;

  public void addUser(User user) {
    user.setId(UserService.userId++);
    userList.add(user);
  }

  public void updateUser(User user) {
    Optional<User> id = userList.stream()
        .filter(t -> t.getId() == user.getId()).findFirst();
    if (id.isPresent()) {
      userList.remove(id.get());
      userList.add(user);
      userList.sort(Comparator.comparingInt(User::getId));
    } else {
      throw new RequestException(HttpStatus.BAD_REQUEST, "用户" + id + "不存在");
    }
  }

  public List<User> getUserList() {
    return userList;
  }

  /**
   * 根据供应商名称或描述进行过滤
   *
   * @param user
   * @return
   */
  public List<User> getUserList(User user) {
    if (user == null) {
      return getUserList();
    }
    return userList.stream().filter(t -> {
      String name = user.getName();
      if (name != null && name.trim().length() > 0) {
        return t.getName().contains(name);
      } else {
        return true;
      }
    }).collect(Collectors.toList());
  }

  public User getUserById(int id) {
    Optional<User> providerOptional = userList.stream()
        .filter(t -> t.getId() == id).findFirst();
    if (providerOptional.isPresent()) {
      return providerOptional.get();
    } else {
      throw new RequestException(HttpStatus.BAD_REQUEST, "用户" + id + "不存在");
    }
  }

  public void deleteUserById(int id) {
    User userById = getUserById(id);
    userList.remove(userById);
  }
}
