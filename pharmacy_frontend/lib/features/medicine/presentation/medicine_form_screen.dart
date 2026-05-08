import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../models/medicine_model.dart';
import '../repository/medicine_repository.dart';
import '../providers/medicine_provider.dart';

class MedicineFormScreen extends ConsumerStatefulWidget {
  final MedicineResponse? medicine;

  const MedicineFormScreen({super.key, this.medicine});

  @override
  ConsumerState<MedicineFormScreen> createState() => _MedicineFormScreenState();
}

class _MedicineFormScreenState extends ConsumerState<MedicineFormScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _genericNameController;
  late TextEditingController _categoryController;
  late TextEditingController _priceController;
  late TextEditingController _sellingPriceController;
  late TextEditingController _stockController;
  late TextEditingController _expiryController;
  late TextEditingController _batchController;
  late TextEditingController _manufacturerController;
  late TextEditingController _minStockLevelController;

  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.medicine?.name ?? '');
    _genericNameController = TextEditingController(text: widget.medicine?.genericName ?? '');
    _categoryController = TextEditingController(text: widget.medicine?.category ?? '');
    _priceController = TextEditingController(text: widget.medicine?.price.toString() ?? '');
    _sellingPriceController = TextEditingController(text: widget.medicine?.sellingPrice?.toString() ?? '');
    _stockController = TextEditingController(text: widget.medicine?.stockQuantity.toString() ?? '');
    _expiryController = TextEditingController(text: widget.medicine?.expiryDate ?? '');
    _batchController = TextEditingController(text: widget.medicine?.batchNumber ?? '');
    _manufacturerController = TextEditingController(text: widget.medicine?.manufacturer ?? '');
    _minStockLevelController = TextEditingController(text: widget.medicine?.minStockLevel?.toString() ?? '10');
  }

  @override
  void dispose() {
    _nameController.dispose();
    _genericNameController.dispose();
    _categoryController.dispose();
    _priceController.dispose();
    _sellingPriceController.dispose();
    _stockController.dispose();
    _expiryController.dispose();
    _batchController.dispose();
    _manufacturerController.dispose();
    _minStockLevelController.dispose();
    super.dispose();
  }

  void _saveMedicine() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final request = MedicineRequest(
          name: _nameController.text,
          genericName: _genericNameController.text.isEmpty ? null : _genericNameController.text,
          category: _categoryController.text,
          price: double.parse(_priceController.text),
          sellingPrice: double.tryParse(_sellingPriceController.text),
          stockQuantity: int.parse(_stockController.text),
          expiryDate: _expiryController.text,
          batchNumber: _batchController.text,
          manufacturer: _manufacturerController.text.isEmpty ? null : _manufacturerController.text,
          minStockLevel: int.tryParse(_minStockLevelController.text),
        );

        if (widget.medicine == null) {
          await ref.read(medicineRepositoryProvider).createMedicine(request);
        } else {
          await ref.read(medicineRepositoryProvider).updateMedicine(widget.medicine!.id!, request);
        }

        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Medicine saved successfully!')),
        );
        // Refresh the medicines list
        ref.invalidate(searchMedicinesProvider);
        context.pop();
      } catch (e) {
        if (!context.mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to save medicine: $e')),
        );
      } finally {
        if (mounted) {
          setState(() => _isLoading = false);
        }
      }
    }
  }

  Widget _buildSection(String title, IconData icon, List<Widget> children) {
    return Container(
      margin: const EdgeInsets.only(bottom: 24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.blueGrey.shade50),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: const Color(0xFF10B981).withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(icon, color: const Color(0xFF10B981), size: 20),
                ),
                const SizedBox(width: 12),
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF0F172A),
                    letterSpacing: -0.5,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            ...children,
          ],
        ),
      ),
    );
  }

  InputDecoration _inputDecoration(String label, IconData icon, {String? prefix, String? hint}) {
    return InputDecoration(
      labelText: label,
      hintText: hint,
      prefixIcon: Icon(icon, size: 20),
      prefixText: prefix,
      filled: true,
      fillColor: const Color(0xFFF8FAFC),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: Colors.blueGrey.shade100),
      ),
      enabledBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: BorderSide(color: Colors.blueGrey.shade100),
      ),
      focusedBorder: OutlineInputBorder(
        borderRadius: BorderRadius.circular(16),
        borderSide: const BorderSide(color: Color(0xFF10B981), width: 2),
      ),
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
    );
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.tryParse(_expiryController.text) ?? DateTime.now(),
      firstDate: DateTime.now(),
      lastDate: DateTime(2101),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: const ColorScheme.light(
              primary: Color(0xFF0F172A),
              onPrimary: Colors.white,
              onSurface: Color(0xFF0F172A),
            ),
          ),
          child: child!,
        );
      },
    );
    if (picked != null) {
      setState(() {
        _expiryController.text = picked.toString().split(' ')[0];
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.medicine != null;
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: Text(isEditing ? 'Update Medicine' : 'New Medicine Entry', style: const TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF0F172A),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              _buildSection(
                'Basic Information',
                Icons.info_rounded,
                [
                  TextFormField(
                    controller: _nameController,
                    decoration: _inputDecoration('Medicine Name', Icons.medication_rounded, hint: 'e.g. Paracetamol'),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _genericNameController,
                    decoration: _inputDecoration('Generic Name', Icons.science_rounded, hint: 'e.g. Acetaminophen'),
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _categoryController,
                    decoration: _inputDecoration('Category', Icons.category_rounded, hint: 'e.g. Analgesic'),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                ],
              ),
              _buildSection(
                'Pricing & Inventory',
                Icons.account_balance_wallet_rounded,
                [
                  Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: _priceController,
                          decoration: _inputDecoration('Cost Price', Icons.attach_money_rounded, prefix: r'$ '),
                          keyboardType: TextInputType.number,
                          validator: (v) => v!.isEmpty ? 'Required' : null,
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: TextFormField(
                          controller: _sellingPriceController,
                          decoration: _inputDecoration('Selling Price', Icons.trending_up_rounded, prefix: r'$ '),
                          keyboardType: TextInputType.number,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    children: [
                      Expanded(
                        child: TextFormField(
                          controller: _stockController,
                          decoration: _inputDecoration('Current Stock', Icons.inventory_2_rounded),
                          keyboardType: TextInputType.number,
                          validator: (v) => v!.isEmpty ? 'Required' : null,
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: TextFormField(
                          controller: _minStockLevelController,
                          decoration: _inputDecoration('Alert Level', Icons.warning_amber_rounded),
                          keyboardType: TextInputType.number,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              _buildSection(
                'Logistics & Expiry',
                Icons.local_shipping_rounded,
                [
                  TextFormField(
                    controller: _expiryController,
                    readOnly: true,
                    onTap: _selectDate,
                    decoration: _inputDecoration('Expiry Date', Icons.calendar_today_rounded),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _batchController,
                    decoration: _inputDecoration('Batch Number', Icons.qr_code_rounded),
                    validator: (v) => v!.isEmpty ? 'Required' : null,
                  ),
                  const SizedBox(height: 16),
                  TextFormField(
                    controller: _manufacturerController,
                    decoration: _inputDecoration('Manufacturer', Icons.business_rounded),
                  ),
                ],
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _saveMedicine,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF0F172A),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    elevation: 0,
                  ),
                  child: _isLoading
                      ? const SizedBox(
                          height: 24,
                          width: 24,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            color: Colors.white,
                          ),
                        )
                      : Text(
                          isEditing ? 'Update Medicine' : 'Register Medicine',
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                        ),
                ),
              ),
              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }
}
