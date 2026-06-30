import {
  Injectable,
  ConflictException,
  NotFoundException,
} from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateFamilyDto } from './dto/create-family.dto';

@Injectable()
export class FamiliesService {
  constructor(private readonly prisma: PrismaService) {}

  async create(parentId: string, dto: CreateFamilyDto) {
    const parent = await this.prisma.parent.findUnique({
      where: { id: parentId },
    });

    if (parent?.familyId) {
      throw new ConflictException('Parent already belongs to a family');
    }

    const family = await this.prisma.family.create({
      data: { name: dto.name },
    });

    await this.prisma.parent.update({
      where: { id: parentId },
      data: { familyId: family.id },
    });

    return family;
  }

  async getMine(parentId: string) {
    const parent = await this.prisma.parent.findUnique({
      where: { id: parentId },
      select: { familyId: true },
    });

    if (!parent?.familyId) {
      throw new NotFoundException('No family found for this account');
    }

    const family = await this.prisma.family.findUnique({
      where: { id: parent.familyId },
      include: {
        children: { select: { id: true, displayName: true, birthYear: true } },
      },
    });

    return family;
  }
}
