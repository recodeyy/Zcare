class AuthResponse {
  final String token;
  final String username;
  final String fullName;
  final String role;
  final String message;

  AuthResponse({
    required this.token,
    required this.username,
    required this.fullName,
    required this.role,
    required this.message,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'] ?? '',
      username: json['username'] ?? '',
      fullName: json['fullName'] ?? '',
      role: json['role'] ?? '',
      message: json['message'] ?? '',
    );
  }
}
