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
  late TextEditingController _categoryController;
  late TextEditingController _priceController;
  late TextEditingController _stockController;
  late TextEditingController _expiryController;
  late TextEditingController _batchController;

  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.medicine?.name ?? '');
    _categoryController = TextEditingController(text: widget.medicine?.category ?? '');
    _priceController = TextEditingController(text: widget.medicine?.price.toString() ?? '');
    _stockController = TextEditingController(text: widget.medicine?.stockQuantity.toString() ?? '');
    _expiryController = TextEditingController(text: widget.medicine?.expiryDate ?? '');
    _batchController = TextEditingController(text: widget.medicine?.batchNumber ?? '');
  }

  @override
  void dispose() {
    _nameController.dispose();
    _categoryController.dispose();
    _priceController.dispose();
    _stockController.dispose();
    _expiryController.dispose();
    _batchController.dispose();
    super.dispose();
  }

  void _saveMedicine() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final request = MedicineRequest(
          name: _nameController.text,
          category: _categoryController.text,
          price: double.parse(_priceController.text),
          stockQuantity: int.parse(_stockController.text),
          expiryDate: _expiryController.text,
          batchNumber: _batchController.text,
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

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.medicine != null;
    return Scaffold(
      appBar: AppBar(title: Text(isEditing ? 'Edit Medicine' : 'Add Medicine')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Name'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _categoryController,
                decoration: const InputDecoration(labelText: 'Category'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _priceController,
                decoration: const InputDecoration(labelText: 'Price'),
                keyboardType: TextInputType.number,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _stockController,
                decoration: const InputDecoration(labelText: 'Stock Quantity'),
                keyboardType: TextInputType.number,
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _expiryController,
                decoration: const InputDecoration(labelText: 'Expiry Date (YYYY-MM-DD)'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _batchController,
                decoration: const InputDecoration(labelText: 'Batch Number'),
                validator: (v) => v!.isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: ElevatedButton(
                  onPressed: _isLoading ? null : _saveMedicine,
                  child: _isLoading ? const CircularProgressIndicator() : Text(isEditing ? 'Update' : 'Save'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
