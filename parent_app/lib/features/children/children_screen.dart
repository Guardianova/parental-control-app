import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'children_repository.dart';

class ChildrenScreen extends ConsumerStatefulWidget {
  const ChildrenScreen({super.key});

  @override
  ConsumerState<ChildrenScreen> createState() => _ChildrenScreenState();
}

class _ChildrenScreenState extends ConsumerState<ChildrenScreen> {
  final _nameController = TextEditingController();
  final _birthYearController = TextEditingController();

  bool _isLoading = false;
  bool _isFetching = true;
  String? _errorMessage;
  List<dynamic> _children = [];

  @override
  void initState() {
    super.initState();
    _loadChildren();
  }

  Future<void> _loadChildren() async {
    try {
      final children =
          await ref.read(childrenRepositoryProvider).getChildren();
      if (mounted) {
        setState(() {
          _children = children;
          _isFetching = false;
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() => _isFetching = false);
      }
    }
  }

  Future<void> _addChild() async {
    if (_nameController.text.trim().isEmpty) {
      setState(() => _errorMessage = 'أدخل اسم الطفل');
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final birthYear = int.tryParse(_birthYearController.text.trim());

      final child = await ref.read(childrenRepositoryProvider).addChild(
            displayName: _nameController.text.trim(),
            birthYear: birthYear,
          );

      if (mounted) {
        setState(() {
          _children.add(child);
          _nameController.clear();
          _birthYearController.clear();
        });

        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('تم إضافة الطفل بنجاح')),
        );
      }
    } catch (e) {
      setState(() => _errorMessage = 'فشل إضافة الطفل، حاول مرة أخرى');
    } finally {
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('الأبناء')),
      body: _isFetching
          ? const Center(child: CircularProgressIndicator())
          : Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  TextField(
                    controller: _nameController,
                    decoration: const InputDecoration(
                      labelText: 'اسم الطفل',
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: _birthYearController,
                    decoration: const InputDecoration(
                      labelText: 'سنة الميلاد (اختياري)',
                    ),
                    keyboardType: TextInputType.number,
                  ),
                  const SizedBox(height: 16),
                  if (_errorMessage != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 12),
                      child: Text(
                        _errorMessage!,
                        style: const TextStyle(color: Colors.red),
                      ),
                    ),
                  FilledButton(
                    onPressed: _isLoading ? null : _addChild,
                    child: _isLoading
                        ? const SizedBox(
                            height: 20,
                            width: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Text('إضافة طفل'),
                  ),
                  const SizedBox(height: 24),
                  const Divider(),
                  const SizedBox(height: 12),
                  if (_children.isEmpty)
                    const Center(
                      child: Text('لا يوجد أبناء مضافون بعد'),
                    )
                  else
                    Expanded(
                      child: ListView.builder(
                        itemCount: _children.length,
                        itemBuilder: (context, index) {
                          final child = _children[index];
                          return ListTile(
                            leading: const CircleAvatar(
                              child: Icon(Icons.person),
                            ),
                            title: Text(child['displayName'] ?? ''),
                            subtitle: child['birthYear'] != null
                                ? Text('سنة الميلاد: ${child['birthYear']}')
                                : null,
                          );
                        },
                      ),
                    ),
                ],
              ),
            ),
    );
  }
}
