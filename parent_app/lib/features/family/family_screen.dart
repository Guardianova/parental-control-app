import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'family_repository.dart';
import '../children/children_screen.dart';

class FamilyScreen extends ConsumerStatefulWidget {
  const FamilyScreen({super.key});

  @override
  ConsumerState<FamilyScreen> createState() => _FamilyScreenState();
}

class _FamilyScreenState extends ConsumerState<FamilyScreen> {
  final _nameController = TextEditingController();

  bool _isLoading = false;
  bool _isFetching = true;
  String? _errorMessage;
  Map<String, dynamic>? _family;

  @override
  void initState() {
    super.initState();
    _loadFamily();
  }

  Future<void> _loadFamily() async {
    try {
      final family = await ref.read(familyRepositoryProvider).getMyFamily();
      if (mounted) {
        setState(() {
          _family = family;
          _isFetching = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _isFetching = false);
      }
    }
  }

  Future<void> _createFamily() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final family = await ref.read(familyRepositoryProvider).createFamily(
            name: _nameController.text.trim().isEmpty
                ? null
                : _nameController.text.trim(),
          );

      if (mounted) {
        setState(() => _family = family);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('تم إنشاء العائلة بنجاح')),
        );
      }
    } catch (e) {
      setState(() => _errorMessage = 'فشل إنشاء العائلة، حاول مرة أخرى');
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_isFetching) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      );
    }

    if (_family != null) {
      return Scaffold(
        appBar: AppBar(title: const Text('عائلتي')),
        body: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'اسم العائلة: ${_family!['name'] ?? 'بدون اسم'}',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const SizedBox(height: 8),
              Text('معرّف العائلة: ${_family!['id']}'),
              const SizedBox(height: 32),
              FilledButton.icon(
                onPressed: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => const ChildrenScreen(),
                    ),
                  );
                },
                icon: const Icon(Icons.people),
                label: const Text('إدارة الأبناء'),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(title: const Text('إنشاء عائلة')),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'لا توجد عائلة مرتبطة بحسابك بعد.',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 24),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'اسم العائلة (اختياري)',
              ),
            ),
            const SizedBox(height: 24),
            if (_errorMessage != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: Text(
                  _errorMessage!,
                  style: const TextStyle(color: Colors.red),
                ),
              ),
            FilledButton(
              onPressed: _isLoading ? null : _createFamily,
              child: _isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('إنشاء العائلة'),
            ),
          ],
        ),
      ),
    );
  }
}
