class RegisterRequest {
  final String username;
  final String password;
  final String fullName;
  final String email;
  final String role; // PHARMACIST, ADMIN, etc.

  RegisterRequest({
    required this.username,
    required this.password,
    required this.fullName,
    required this.email,
    required this.role,
  });

  Map<String, dynamic> toJson() {
    return {
      'username': username,
      'password': password,
      'fullName': fullName,
      'email': email,
      'role': role,
    };
  }
}
