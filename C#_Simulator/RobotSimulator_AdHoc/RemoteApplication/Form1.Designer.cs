namespace RemoteApplication
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.forwardButton = new System.Windows.Forms.Button();
            this.reverseButton = new System.Windows.Forms.Button();
            this.leftButton = new System.Windows.Forms.Button();
            this.stopButton = new System.Windows.Forms.Button();
            this.rightButton = new System.Windows.Forms.Button();
            this.frontSensorLabel = new System.Windows.Forms.Label();
            this.leftSensorLabel = new System.Windows.Forms.Label();
            this.rightSensorLabel = new System.Windows.Forms.Label();
            this.rearSensorLabel = new System.Windows.Forms.Label();
            this.compassLabel = new System.Windows.Forms.Label();
            this.autonomousButton = new System.Windows.Forms.Button();
            this.button1 = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // forwardButton
            // 
            this.forwardButton.Location = new System.Drawing.Point(263, 38);
            this.forwardButton.Name = "forwardButton";
            this.forwardButton.Size = new System.Drawing.Size(75, 23);
            this.forwardButton.TabIndex = 0;
            this.forwardButton.Text = "Forward";
            this.forwardButton.UseVisualStyleBackColor = true;
            this.forwardButton.Click += new System.EventHandler(this.forwardButton_Click);
            // 
            // reverseButton
            // 
            this.reverseButton.Location = new System.Drawing.Point(263, 131);
            this.reverseButton.Name = "reverseButton";
            this.reverseButton.Size = new System.Drawing.Size(75, 23);
            this.reverseButton.TabIndex = 1;
            this.reverseButton.Text = "Reverse";
            this.reverseButton.UseVisualStyleBackColor = true;
            this.reverseButton.Click += new System.EventHandler(this.reverseButton_Click);
            // 
            // leftButton
            // 
            this.leftButton.Location = new System.Drawing.Point(162, 83);
            this.leftButton.Name = "leftButton";
            this.leftButton.Size = new System.Drawing.Size(75, 23);
            this.leftButton.TabIndex = 2;
            this.leftButton.Text = "Left";
            this.leftButton.UseVisualStyleBackColor = true;
            this.leftButton.Click += new System.EventHandler(this.leftButton_Click);
            // 
            // stopButton
            // 
            this.stopButton.Location = new System.Drawing.Point(263, 83);
            this.stopButton.Name = "stopButton";
            this.stopButton.Size = new System.Drawing.Size(75, 23);
            this.stopButton.TabIndex = 3;
            this.stopButton.Text = "Stop";
            this.stopButton.UseVisualStyleBackColor = true;
            this.stopButton.Click += new System.EventHandler(this.stopButton_Click);
            // 
            // rightButton
            // 
            this.rightButton.Location = new System.Drawing.Point(363, 83);
            this.rightButton.Name = "rightButton";
            this.rightButton.Size = new System.Drawing.Size(75, 23);
            this.rightButton.TabIndex = 4;
            this.rightButton.Text = "Right";
            this.rightButton.UseVisualStyleBackColor = true;
            this.rightButton.Click += new System.EventHandler(this.rightButton_Click);
            // 
            // frontSensorLabel
            // 
            this.frontSensorLabel.AutoSize = true;
            this.frontSensorLabel.Location = new System.Drawing.Point(279, 192);
            this.frontSensorLabel.Name = "frontSensorLabel";
            this.frontSensorLabel.Size = new System.Drawing.Size(46, 17);
            this.frontSensorLabel.TabIndex = 5;
            this.frontSensorLabel.Text = "label1";
            // 
            // leftSensorLabel
            // 
            this.leftSensorLabel.AutoSize = true;
            this.leftSensorLabel.Location = new System.Drawing.Point(214, 228);
            this.leftSensorLabel.Name = "leftSensorLabel";
            this.leftSensorLabel.Size = new System.Drawing.Size(46, 17);
            this.leftSensorLabel.TabIndex = 6;
            this.leftSensorLabel.Text = "label2";
            // 
            // rightSensorLabel
            // 
            this.rightSensorLabel.AutoSize = true;
            this.rightSensorLabel.Location = new System.Drawing.Point(342, 228);
            this.rightSensorLabel.Name = "rightSensorLabel";
            this.rightSensorLabel.Size = new System.Drawing.Size(46, 17);
            this.rightSensorLabel.TabIndex = 7;
            this.rightSensorLabel.Text = "label3";
            // 
            // rearSensorLabel
            // 
            this.rearSensorLabel.AutoSize = true;
            this.rearSensorLabel.Location = new System.Drawing.Point(279, 269);
            this.rearSensorLabel.Name = "rearSensorLabel";
            this.rearSensorLabel.Size = new System.Drawing.Size(46, 17);
            this.rearSensorLabel.TabIndex = 8;
            this.rearSensorLabel.Text = "label4";
            // 
            // compassLabel
            // 
            this.compassLabel.AutoSize = true;
            this.compassLabel.Location = new System.Drawing.Point(279, 228);
            this.compassLabel.Name = "compassLabel";
            this.compassLabel.Size = new System.Drawing.Size(46, 17);
            this.compassLabel.TabIndex = 9;
            this.compassLabel.Text = "label5";
            // 
            // autonomousButton
            // 
            this.autonomousButton.Location = new System.Drawing.Point(520, 38);
            this.autonomousButton.Name = "autonomousButton";
            this.autonomousButton.Size = new System.Drawing.Size(104, 23);
            this.autonomousButton.TabIndex = 10;
            this.autonomousButton.Text = "Autonomous";
            this.autonomousButton.UseVisualStyleBackColor = true;
            this.autonomousButton.Click += new System.EventHandler(this.autonomousButton_Click);
            // 
            // button1
            // 
            this.button1.Enabled = false;
            this.button1.Location = new System.Drawing.Point(520, 324);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(75, 23);
            this.button1.TabIndex = 11;
            this.button1.Text = "CSE459";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Visible = false;
            this.button1.Click += new System.EventHandler(this.button1_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(671, 387);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.autonomousButton);
            this.Controls.Add(this.compassLabel);
            this.Controls.Add(this.rearSensorLabel);
            this.Controls.Add(this.rightSensorLabel);
            this.Controls.Add(this.leftSensorLabel);
            this.Controls.Add(this.frontSensorLabel);
            this.Controls.Add(this.rightButton);
            this.Controls.Add(this.stopButton);
            this.Controls.Add(this.leftButton);
            this.Controls.Add(this.reverseButton);
            this.Controls.Add(this.forwardButton);
            this.Name = "Form1";
            this.Text = "Client Application";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button forwardButton;
        private System.Windows.Forms.Button reverseButton;
        private System.Windows.Forms.Button leftButton;
        private System.Windows.Forms.Button stopButton;
        private System.Windows.Forms.Button rightButton;
        private System.Windows.Forms.Label frontSensorLabel;
        private System.Windows.Forms.Label leftSensorLabel;
        private System.Windows.Forms.Label rightSensorLabel;
        private System.Windows.Forms.Label rearSensorLabel;
        private System.Windows.Forms.Label compassLabel;
        private System.Windows.Forms.Button autonomousButton;
        private System.Windows.Forms.Button button1;
    }
}

