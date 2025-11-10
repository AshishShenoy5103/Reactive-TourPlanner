import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthAdminService } from '../../services/authAdmin.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-login',
  imports: [FormsModule, CommonModule, RouterModule],
  templateUrl: './admin-login.component.html',
  styleUrl: './admin-login.component.css'
})
export class AdminLoginComponent {
  email: string = '';
  password: string = '';
  errorMessage: string = '';

  constructor(private authAdminService: AuthAdminService, private router: Router) {}

  onSubmit() {
    // console.log(this.email);
    // console.log(this.password);

    this.authAdminService.login(this.email, this.password).subscribe({
      next: (result) => {
        if (result.error) {
          // GraphQL mutation returned an error message
          this.errorMessage = result.error;
          alert(this.errorMessage);
        } else if (result.token) {
          // Successful login
          this.router.navigate(['/admin/dashboard']);
        } else {
          // Unexpected case
          alert('Something went wrong');
        }
      },
      error: (err) => {
        // Network or Apollo errors
        console.error(err);
        alert('Something went wrong');
      }
    });
  }
}
