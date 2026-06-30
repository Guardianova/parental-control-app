import {
  Controller,
  Post,
  Get,
  Body,
  UseGuards,
  Req,
} from '@nestjs/common';
import { Request } from 'express';
import { FamiliesService } from './families.service';
import { CreateFamilyDto } from './dto/create-family.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

interface AuthenticatedRequest extends Request {
  user: { id: string };
}

@UseGuards(JwtAuthGuard)
@Controller('families')
export class FamiliesController {
  constructor(private readonly familiesService: FamiliesService) {}

  @Post()
  create(@Req() req: AuthenticatedRequest, @Body() dto: CreateFamilyDto) {
    return this.familiesService.create(req.user.id, dto);
  }

  @Get('me')
  getMine(@Req() req: AuthenticatedRequest) {
    return this.familiesService.getMine(req.user.id);
  }
}
