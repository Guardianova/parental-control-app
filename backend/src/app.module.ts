import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { PrismaModule } from './prisma/prisma.module';
import { AuthModule } from './auth/auth.module';
import { FamiliesModule } from './families/families.module';
import { ChildrenModule } from './children/children.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    PrismaModule,
    AuthModule,
    FamiliesModule,
    ChildrenModule,
  ],
})
export class AppModule {}
