class AuthResponse {
  final String token;
  final String username;
  final String role;

  AuthResponse({
    required this.token,
    required this.username,
    required this.role,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'] ?? '',
      username: json['username'] ?? '',
      role: json['role'] ?? '',
    );
  }
}
