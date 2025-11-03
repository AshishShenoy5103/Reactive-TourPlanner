import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-register',
  imports: [FormsModule, RouterModule, CommonModule],
  templateUrl: './user-register.component.html',
  styleUrl: './user-register.component.css'
})
export class UserRegisterComponent {
  firstName = '';
  lastName = '';
  email = '';
  password = '';
  aadharNumber = '';
  phoneNumber = '';
  city = '';

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(form: any) {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    const userRegisterDTO = {
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      password: this.password,
      aadharNumber: this.aadharNumber,
      phoneNumber: this.phoneNumber,
      city: this.city
    };

    this.authService.register(userRegisterDTO).subscribe({
      next: (response) => {
        if (response.registerUser) {
          alert('Registration successful!');
          this.router.navigate(['/login/user']);
        } else {
          alert('Registration failed!');
        }
      },
      error: (err) => {
        console.error(err);
        alert(err.message || 'Registration failed! Please try again.');
      }
    });
  }
}
