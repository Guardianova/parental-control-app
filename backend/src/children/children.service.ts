import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateChildDto } from './dto/create-child.dto';

@Injectable()
export class ChildrenService {
  constructor(private readonly prisma: PrismaService) {}

  private async getFamilyIdOrFail(parentId: string): Promise<string> {
    const parent = await this.prisma.parent.findUnique({
      where: { id: parentId },
      select: { familyId: true },
    });

    if (!parent?.familyId) {
      throw new NotFoundException('No family found for this account');
    }

    return parent.familyId;
  }

  async create(parentId: string, dto: CreateChildDto) {
    const familyId = await this.getFamilyIdOrFail(parentId);

    return this.prisma.child.create({
      data: {
        familyId,
        displayName: dto.displayName,
        birthYear: dto.birthYear,
      },
    });
  }

  async findAll(parentId: string) {
    const familyId = await this.getFamilyIdOrFail(parentId);

    return this.prisma.child.findMany({
      where: { familyId },
      orderBy: { createdAt: 'asc' },
    });
  }
}
